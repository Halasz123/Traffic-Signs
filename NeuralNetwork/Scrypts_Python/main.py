### https://www.kaggle.com/sohelranaccselab/99-80-acc-on-german-traffic-sign-recognition/comments
import PIL
import matplotlib
import numpy as np  # linear algebra
import pandas as pd  # data processing, CSV file I/O (e.g. pd.read_csv)
import tensorflow as tf
import matplotlib.pyplot as plt
import os

import PIL.Image as Image
import cv2
import seaborn as sns
import warnings

from keras_preprocessing.image import ImageDataGenerator
from skimage.exposure import exposure
from tensorflow.python.keras.optimizer_v2.adam import Adam

from trafficsignmodel import TrafficSignNet

from keras.models import Sequential
from keras.layers import Dense, Conv2D, MaxPool2D, Flatten, BatchNormalization
from keras.layers import Dropout

warnings.simplefilter(action='ignore', category=FutureWarning)

# plotly.offline.init_notebook_mode(True)

dataset_dir = '/Users/botondhalasz/Desktop/Allamvizsga/NeuralNetwork/Kagle_trye/archive/'
meta_info = os.path.join(dataset_dir, 'Meta.csv')
train_csv_path = os.path.join(dataset_dir, 'Train.csv')
test_csv_path = os.path.join(dataset_dir, 'Test.csv')
labels = ['Begin of a speed limit (20km/h)',
'Begin of a speed limit (30km/h)',
'Begin of a speed limit (50km/h)',
'Begin of a speed limit (60km/h)',
'Begin of a speed limit (70km/h)',
'Begin of a speed limit (80km/h)',
'End of speed limit (80km/h)',
'Begin of a speed limit (100km/h)',
'Begin of a speed limit(120km/h)',
'Overtaking prohibited',
'Overtaking prohibited for trucks',
'Warning for a crossroad side roads on the left and right',
'Begin of priority road',
'Give way to all drivers',
'Stop and give way to all drivers',
'Entry prohibited',
'Trucks prohibited',
'Entry prohibited(road with one way traffic)',
'Warning for a danger with no specific traffic sign',
'Warning for a curve to the left',
'Warning for a curve to the right',
'Warning for a double curve',
'Warning for a bad road surface',
'Warning for a slippery road surface',
'Warning for a road narrowing on the right',
'Warning for roadworks',
'Warning for traffic light',
'Warning for a crossing for pedestrians',
'Warning for children',
'Warning for cyclists',
'Beware of ice/snow',
'Warning for crossing deer',
'End of the limits',
'Turning right mandatory',
'Turning left mandatory',
'Driving straight ahead mandatory',
'Driving straight ahead or turning right mandatory',
'Driving straight ahead or turning left mandatory',
'Passing right mandatory',
'Passing left mandatory',
'Mandatory direction of the roundabout',
'End of the overtaking prohibition',
'End of overtaking limit for trac']

train_data_color = '#0f7b8e'
test_data_color = '#630f8e'

trainDf = pd.read_csv(train_csv_path)
X_train = trainDf['Path']
y_train = trainDf.ClassId

# print(trainDf)

testDf = pd.read_csv(test_csv_path)
metaDf = pd.read_csv(meta_info)

print(trainDf.shape)


trainDf['Path'] = list(map(lambda x: os.path.join(dataset_dir,x.lower()), trainDf['Path']))
testDf['Path'] = list(map(lambda x: os.path.join(dataset_dir,x.lower()), testDf['Path']))
metaDf['Path'] = list(map(lambda x: os.path.join(dataset_dir,x.lower()), metaDf['Path']))

trainDf.sample(3)


def showDatasetBalance():
    fig, axs = plt.subplots(1, 2, sharex=True, sharey=True, figsize=(25, 6))
    axs[0].set_title('Train classes distribution')
    axs[0].set_xlabel('Class')
    axs[0].set_ylabel('Count')
    axs[1].set_title('Test classes distribution')
    axs[1].set_xlabel('Class')
    axs[1].set_ylabel('Count')

    sns.countplot(trainDf.ClassId, ax=axs[0])
    sns.countplot(testDf.ClassId, ax=axs[1])
    axs[0].set_xlabel('Class ID');
    axs[1].set_xlabel('Class ID');

    fig.show()

showDatasetBalance()

def showImageSizeDistribution():
    trainDfDpiSubset = trainDf[(trainDf.Width < 80) & (trainDf.Height < 80)];
    testDfDpiSubset = testDf[(testDf.Width < 80) & (testDf.Height < 80)];

    g = sns.JointGrid(x="Width", y="Height", data=trainDfDpiSubset)
    sns.kdeplot(trainDfDpiSubset.Width, trainDfDpiSubset.Height, cmap="Reds",
                shade=False, thresh=False, ax=g.ax_joint)
    sns.kdeplot(testDfDpiSubset.Width, testDfDpiSubset.Height, cmap="Blues",
                shade=False, thresh=False, ax=g.ax_joint)
    sns.distplot(trainDfDpiSubset.Width, kde=True, hist=False, color="r", ax=g.ax_marg_x, label='Train distribution')
    sns.distplot(testDfDpiSubset.Width, kde=True, hist=False, color="b", ax=g.ax_marg_x, label='Test distribution')
    sns.distplot(trainDfDpiSubset.Width, kde=True, hist=False, color="r", ax=g.ax_marg_y, vertical=True)
    sns.distplot(testDfDpiSubset.Height, kde=True, hist=False, color="b", ax=g.ax_marg_y, vertical=True)
    g.fig.set_figwidth(25)
    g.fig.set_figheight(8)
    plt.show()

#
# def showClasses(metaDf=None):
#     sns.set_style()
#     rows = 6
#     cols = 8
#     fig, axs = plt.subplots(rows, cols, sharex=True, sharey=True, figsize=(25, 12))
#     plt.subplots_adjust(left=None, bottom=None, right=None, top=0.9, wspace=None, hspace=None)
#     metaDf = metaDf.sort_values(by=['ClassId'])
#
#     idx = 0
#     for i in range(rows):
#         for j in range(cols):
#             if idx > 42:
#                 break
#
#             img = cv2.imread(metaDf["Path"].tolist()[idx], cv2.IMREAD_UNCHANGED)
#             img[np.where(img[:, :, 3] == 0)] = [255, 255, 255, 255]
#             img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
#             img = cv2.resize(img, (60, 60))
#
#             axs[i, j].imshow(img)
#             axs[i, j].set_facecolor('xkcd:salmon')
#             axs[i, j].set_facecolor((1.0, 0.47, 0.42))
#             axs[i, j].set_title(labels[int(metaDf["ClassId"].tolist()[idx])])
#             axs[i, j].get_xaxis().set_visible(False)
#             axs[i, j].get_yaxis().set_visible(False)
#             idx += 1
#     fig.show()
#
#
# def sampleVisualisation():
#     rows = 6
#     cols = 8 + 4
#     fig, axs = plt.subplots(rows, cols, sharex=True, sharey=True, figsize=(25, 12))
#     plt.subplots_adjust(left=None, bottom=None, right=None, top=0.9, wspace=None, hspace=None)
#     visualize = trainDf.sample(rows * cols)
#
#     idx = 0
#     for i in range(rows):
#         for j in range(cols):
#             img = cv2.imread(visualize["Path"].tolist()[idx])
#             img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
#             img = cv2.resize(img, (60, 60))
#             axs[i, j].imshow(img)
#             axs[i, j].set_title(labels[int(visualize["ClassId"].tolist()[idx])])
#             axs[i, j].get_xaxis().set_visible(False)
#             axs[i, j].get_yaxis().set_visible(False)
#             idx += 1
#     fig.show()
#

#showDatasetBalance()
#showImageSizeDistribution()
# showClasses(metaDf)
# sampleVisualisation()

# print("TF version:", tf.__version__)
# print("Hub version:", hub.__version__)
# print("GPU is", "available" if tf.test.is_gpu_available() else "NOT AVAILABLE")


# Reading the input images and putting them into a numpy array¶

data = []
labels = []

height = 32
width = 32
channels = 3
classes = 43
n_inputs = height * width * channels

for i in range(classes):
    path = "/Users/botondhalasz/Desktop/Allamvizsga/NeuralNetwork/Kagle_trye/archive/Train/{0}/".format(i)
    print(path)
    Class = os.listdir(path)
    j = 0
    for a in Class:
        try:
            image = PIL.Image.open(path + a)

           # image = exposure.equalize_adapthist(image, clip_limit=0.1)
           # image_from_array = Image.fromarray(image, 'RGB')

            resized_image = image.resize((height, width))
            data.append(np.array(resized_image))
            labels.append(i)
        except AttributeError:
            print(" ")

Cells = np.array(data)
labels = np.array(labels)

# Randomize the order of the input images
s = np.arange(Cells.shape[0])
np.random.seed(classes-1)
np.random.shuffle(s)
Cells = Cells[s]
labels = labels[s]

(X_train, X_val) = Cells[int(0.20 * len(labels)):], Cells[:int(0.20 * len(labels))]

X_train = X_train.astype('float32') / 255.0
X_val = X_val.astype('float32') / 255.0

(y_train, y_val) = labels[int(0.20 * len(labels)):], labels[:int(0.20 * len(labels))]

from keras.utils import to_categorical

y_train = to_categorical(y_train, classes)
y_val = to_categorical(y_val, classes)

print("Trains shape:  ", X_train.shape[1:])


def get_model():
    model = Sequential()
    model.add(Conv2D(filters=32, kernel_size=(5, 5), activation='relu', input_shape=X_train.shape[1:]))
    model.add(Conv2D(filters=32, kernel_size=(5, 5), activation='relu'))
    model.add(MaxPool2D(pool_size=(2, 2)))
    model.add(Dropout(rate=0.3))
    model.add(Conv2D(filters=64, kernel_size=(3, 3), activation='relu'))
    model.add(Conv2D(filters=128, kernel_size=(2, 2), activation='relu'))
    model.add(MaxPool2D(pool_size=(2, 2)))
    model.add(Dropout(rate=0.3))
    model.add(Flatten())
    model.add(Dense(512, activation='relu'))
    model.add(Dropout(rate=0.5))
    model.add(Dense(classes, activation='softmax'))
    return model


aug = ImageDataGenerator(
    rotation_range=10,
    zoom_range=0.15,
    width_shift_range=0.1,
    height_shift_range=0.1,
    shear_range=0.15,
    horizontal_flip=False,
    vertical_flip=False,
    fill_mode="nearest")

classTotals = X_train.sum(axis=0)
classWeight = classTotals.max() / classTotals

NUM_EPOCHS = 200
INIT_LR = 1e-5
BS = 32

cnn_model = get_model()
cnn_model.save(filepath='/Users/botondhalasz/Desktop/Allamvizsga/NeuralNetwork/SavedModels/FirstModel200EPIL.model')
opt = Adam(lr=INIT_LR, decay=INIT_LR / (NUM_EPOCHS * 0.5))

cnn_model.compile(
    loss='categorical_crossentropy',
    optimizer='Adam',
    metrics=['accuracy']
)
cnn_model.summary()

X_test = X_val
y_test = y_val

epochs = 200

# print("[INFO] training network...")
# H = cnn_model.fit_generator(
#     aug.flow(X_train, y_train, batch_size=BS),
#     validation_data=(X_test, y_test),
#     steps_per_epoch=X_train.shape[0] // BS,
#     epochs=NUM_EPOCHS,
#     class_weight=classWeight,
#     verbose=1)

history = cnn_model.fit(X_train,
                        y_train,
                        batch_size=32,
                        epochs=epochs,
                        validation_data=(X_test, y_test),
                        verbose=1)

cnn_model.save("FirstModel200EPIL.h5")

scores = cnn_model.evaluate(X_test, y_test, verbose=0)
print("%s: %.2f%%" % (cnn_model.metrics_names[1], scores[1] * 100))

plt.figure(0)
plt.plot(history.history['accuracy'], label='training accuracy')
plt.plot(history.history['val_accuracy'], label='val accuracy')
plt.title('Accuracy')
plt.xlabel('epochs')
plt.ylabel('accuracy')
plt.legend()
plt.show()

plt.figure(1)
plt.plot(history.history['loss'], label='training loss')
plt.plot(history.history['val_loss'], label='val loss')
plt.title('Loss')
plt.xlabel('epochs')
plt.ylabel('loss')
plt.legend()
plt.show()
