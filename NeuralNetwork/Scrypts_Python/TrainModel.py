#!/Library/Frameworks/Python.framework/Versions/3.6/bin/python3.8
import os, re, timeit, json, warnings, random

import numpy as np
import pandas as pd
import tensorflow as tf
import matplotlib.pyplot as plt
import time

from PIL import Image
from matplotlib.image import imread
##from kaggle_datasets import KaggleDatasets
from sklearn.metrics import accuracy_score
from sklearn.model_selection import train_test_split
from skimage import exposure

warnings.filterwarnings("ignore")

print("Tensorflow version " + tf.__version__)

device_name = tf.test.gpu_device_name()
if "GPU" not in device_name:
    print("GPU device not found")
print('Found GPU at: {}'.format(device_name))

IMAGE_SIZE = 28
EPOCHS = 25
BATCH_SIZE = 32
AUTOTUNE = tf.data.experimental.AUTOTUNE

# storing data directory path
data = "../Kagle_trye/archive"
# reading Train.csv
train_csv = pd.read_csv('../Kagle_trye/archive/Train.csv')
# looking at the first 5 rows of Train.csv
print(train_csv.head())

test_csv = pd.read_csv('../Kagle_trye/archive/Test.csv')
# looking at the first 5 rows of Test.csv
print(test_csv.head())

CLASSES = train_csv["ClassId"].nunique()
print("Number of unique classes: ", train_csv["ClassId"].nunique())

# visualising how many images of each class exist in the train dataset
plt.figure(figsize=(20, 10))
train_csv["ClassId"].value_counts(sort=True).plot.bar()
plt.show()

# creating a list of train and test images path
train_img_path = data+'/'+train_csv["Path"].values
test_img_path = data+'/'+test_csv["Path"].values

# a function that will plot a grid of random 9 images and accepts image path
def plot_img(img_path):
    plt.figure(figsize=(15,15))
    for i in range(1,10):
        plt.subplot(3,3,i)
        random_img_path = random.choice(img_path)
        rand_img = imread(random_img_path)
        plt.imshow(rand_img)
        plt.grid(b=None)
        plt.xlabel(rand_img.shape[1], fontsize = 20)#width of image
        plt.ylabel(rand_img.shape[0], fontsize = 20)#height of image

plt.figure(2)
# visualising 9 train images
plot_img(train_img_path)
plt.suptitle('Train Images')
plt.show()
plt.figure(3)
# visualising 9 test images
plot_img(test_img_path)
plt.suptitle('Test Images')
plt.show()


def img_to_array(data_path, csv):
    data = []
    labels = csv.ClassId
    for i in range(len(data_path)):
        image = tf.io.read_file(data_path[i])
        image = tf.image.decode_jpeg(image, channels=3)
        image = tf.cast(image, tf.float32) / 256.0
        image = tf.image.resize(image, [IMAGE_SIZE, IMAGE_SIZE])
        image = exposure.equalize_adapthist(image, clip_limit=0.1)
        # adding image to arrays
        data.append(image)

    return data, labels


data, labels = img_to_array(train_img_path, train_csv)

# changing the list to a numpy array
X = np.array(data)
y = np.array(labels)

print("----Before Splitting----")
print("X.shape", X.shape)
print("y.shape", y.shape)

# splitting the train data into train and validation
X_train, X_val, y_train, y_val = train_test_split( X, y, test_size=0.20, random_state=42, shuffle=True)

# checking the shape of train and validation data after splitting
print("----After Splitting----")
print("X_train.shape", X_train.shape)
print("X_valid.shape", X_val.shape)
print("y_train.shape", y_train.shape)
print("y_valid.shape", y_val.shape)

# one-hot encoding the target labels
y_train = tf.keras.utils.to_categorical(y_train, CLASSES)
y_val = tf.keras.utils.to_categorical(y_val, CLASSES)
# checking the shape of train and validation data after one-hot encoding
print("----After 1-hot encoding----")
print("y_train.shape", y_train.shape)
print("y_valid.shape", y_val.shape)


from keras.callbacks import *
from CLR.clr_callback import *

# using the triangular learning rate policy
clr_triangular = CyclicLR(mode='triangular')

learning_rate_reduction = tf.keras.callbacks.ReduceLROnPlateau(
    monitor = 'val_loss',
    patience = 3,
    verbose = 1,
    factor = 0.5,
    min_lr = 1e-6
)

# applying random transformations to the images
augment = tf.keras.preprocessing.image.ImageDataGenerator(
    rotation_range=10,
    zoom_range=0.15,
    width_shift_range=0.1,
    height_shift_range=0.1,
    shear_range=0.15,
    horizontal_flip=False,
    vertical_flip=False,
    fill_mode="nearest")

# defining model-resnet50
resnet_model = tf.keras.applications.ResNet50(
    weights='imagenet',
    include_top=False
)

x = resnet_model.output
x = tf.keras.layers.GlobalAveragePooling2D()(x)
x = tf.keras.layers.Dense(512,activation='relu')(x)
predictions = tf.keras.layers.Dense(
    CLASSES,
    activation='softmax'
)(x)


resnet50_model = tf.keras.models.Model(
    inputs= resnet_model.input,
    outputs=predictions
)

resnet50_model.save(filepath='/SavedModels/Resnet50.model')
resnet50_model.compile(
    loss='categorical_crossentropy',
    optimizer=tf.keras.optimizers.SGD(lr=1e-4, momentum=0.9),
    metrics=['accuracy']
)

resnet50_model.summary()


startTime = timeit.default_timer()
history_lrr = resnet50_model.fit(
    augment.flow(X_train, y_train, batch_size=BATCH_SIZE),
    epochs=50,
    validation_data=(X_val, y_val),
    callbacks = [learning_rate_reduction]
)
resnet50_model.save("Resnet50.h5")
elapsedTime = timeit.default_timer() - startTime
print("Time taken for the Network to train : ",elapsedTime)

#Display of the accuracy and the loss values
plt.figure(4)
plt.plot(history_lrr.history['accuracy'], label='training accuracy')
plt.plot(history_lrr.history['val_accuracy'], label='val accuracy')
plt.title('Accuracy')
plt.xlabel('epochs')
plt.ylabel('accuracy')
plt.legend()

plt.figure(5)
plt.plot(history_lrr.history['loss'], label='training loss')
plt.plot(history_lrr.history['val_loss'], label='val loss')
plt.title('Loss')
plt.xlabel('epochs')
plt.ylabel('loss')
plt.legend()

startTime = timeit.default_timer()
history_clr = resnet50_model.fit(
    augment.flow(X_train, y_train, batch_size=BATCH_SIZE),
    epochs=50,
    validation_data=(X_val, y_val),
    callbacks = [clr_triangular]
)
elapsedTime = timeit.default_timer() - startTime
print("Time taken for the Network to train : ",elapsedTime)


#Display of the accuracy and the loss values
plt.figure(6)
plt.plot(history_clr.history['accuracy'], label='training accuracy')
plt.plot(history_clr.history['val_accuracy'], label='val accuracy')
plt.title('Accuracy')
plt.xlabel('epochs')
plt.ylabel('accuracy')
plt.legend()

plt.figure(7)
plt.plot(history_clr.history['loss'], label='training loss')
plt.plot(history_clr.history['val_loss'], label='val loss')
plt.title('Loss')
plt.xlabel('epochs')
plt.ylabel('loss')
plt.legend()

X_test, y_test = img_to_array(test_img_path, test_csv)

# changing the list to a numpy array
X_test = np.array(X_test)
y_test = np.array(y_test)

# predictions
prediction = resnet50_model.predict(X_test).argmax(axis=1)

# accuracy
acc = accuracy_score(y_test, prediction)
print("Accuracy: ", acc)