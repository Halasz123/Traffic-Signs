###https://towardsdatascience.com/transfer-learning-using-mobilenet-and-keras-c75daf7ff299
import os

import cv2
import keras
from keras import backend as K
from keras.layers.core import Dense, Activation
from keras.optimizers import Adam
from keras.metrics import categorical_crossentropy
from keras.preprocessing.image import ImageDataGenerator
from keras.preprocessing import image
from keras.models import Model
from keras.applications import imagenet_utils
from keras.layers import Dense, GlobalAveragePooling2D
from keras.applications import MobileNet
from keras.applications.mobilenet import preprocess_input
import numpy as np
from IPython.display import Image
import PIL.Image as ImagePil
from keras.optimizers import Adam


labels = ['Begin of a speed limit', 'End of a speed limit',
          'Overtaking prohibited', 'Overtaking prohibited for trucks',
          'Warning for a crossroad side roads on the left and right',
          'Begin of priority road', 'Give way to all drivers',
          'Stop and give way to all drivers',
          'Entry prohibited',
          'Trucks prohibited',
          'Entry prohibited(road with one way traffic)',
          'Warning for a danger with no specific traffic sign',
          'Warning for a curve to the left', 'Warning for a curve to the right',
          'Warning for a double curve', 'Warning for a bad road surface',
          'Warning for a slippery road surface',
          'Warning for a road narrowing on the right',
          'Warning for roadworks', 'Warning for traffic light',
          'Warning for a crossing for pedestrians', 'Warning for children',
          'Warning for cyclists', 'Snow', 'Warning for crossing deer',
          'End of the limits', 'Turning right mandatory', 'Turning left mandatory',
          'Driving straight ahead mandatory', 'Driving straight ahead or turning right mandatory',
          'Driving straight ahead or turning left mandatory', 'Passing right mandatory', 'Passing left mandatory',
          'Mandatory direction of the roundabout',
          'End of the overtaking prohibition',
          'End of overtaking limit for track']

mobile = keras.applications.mobilenet.MobileNet()


def prepare_image(file):
    img_path = ''
    img = image.load_img(img_path + file, target_size=(224, 224))
    img_array = image.img_to_array(img)
    img_array_expanded_dims = np.expand_dims(img_array, axis=0)
    return keras.applications.mobilenet.preprocess_input(img_array_expanded_dims)


Image(filename='../Dataset/Test/00000.png')
preprocessed_image = prepare_image('../Dataset/Test/00000.png')
predictions = mobile.predict(preprocessed_image)
results = imagenet_utils.decode_predictions(predictions)
print(results)

base_model = MobileNet(weights='imagenet',
                       include_top=False)  # imports the mobilenet model and discards the last 1000 neuron layer.

x = base_model.output
x = GlobalAveragePooling2D()(x)
x = Dense(1024, activation='relu')(
    x)  # we add dense layers so that the model can learn more complex functions and classify for better results.
x = Dense(1024, activation='relu')(x)  # dense layer 2
x = Dense(512, activation='relu')(x)  # dense layer 3
preds = Dense(2, activation='softmax')(x)  # final layer with softmax activation

model = Model(inputs=base_model.input, outputs=preds)

# for i, layer in enumerate(model.layers):
#     print(i, layer.name)

for layer in model.layers[:20]:
    layer.trainable = False
for layer in model.layers[20:]:
    layer.trainable = True

#train_datagen = ImageDataGenerator(preprocessing_function=preprocess_input)  # included in our dependencies

# train_generator = train_datagen.flow_from_directory(
#     '/Users/botondhalasz/Desktop/Allamvizsga/NeuralNetwork/Dataset/Train',
#     target_size=(224, 224),
#     color_mode='rgb',
#     batch_size=32,
#     class_mode='categorical',
#     shuffle=True)


data = []
labels = []

height = 32
width = 32
channels = 10
classes = 36
n_inputs = height * width * channels

for i in range(classes):
    path = "/Users/botondhalasz/Desktop/Allamvizsga/NeuralNetwork/Dataset/Train/{0}/".format(i)
    print(path)
    Class = os.listdir(path)
    for a in Class:
        try:
            image = cv2.imread(path + a)
            image_from_array = ImagePil.fromarray(image, 'RGB')
            size_image = image_from_array.resize((height, width))
            data.append(np.array(size_image))
            labels.append(i)
        except AttributeError:
            print("-")

Cells = np.array(data)
labels = np.array(labels)

# Randomize the order of the input images
s = np.arange(Cells.shape[0])
np.random.seed(classes - 1)
np.random.shuffle(s)
Cells = Cells[s]
labels = labels[s]

(X_train, X_val) = Cells[int(0.20 * len(labels)):], Cells[:int(0.20 * len(labels))]

X_train = X_train.astype('float32') / 255
X_val = X_val.astype('float32') / 255

(y_train, y_val) = labels[int(0.20 * len(labels)):], labels[:int(0.20 * len(labels))]

from keras.utils import to_categorical

y_train = to_categorical(y_train, classes)
y_val = to_categorical(y_val, classes)

model.compile(optimizer='Adam', loss='categorical_crossentropy', metrics=['accuracy'])
model.summary()
# Adam optimizer
# loss function will be categorical cross entropy
# evaluation metric will be accuracy

# step_size_train = train_generator.n//train_generator.batch_size
# model.fit(
#                    steps_per_epoch=step_size_train,
#                    epochs=10)

X_test = X_val
y_test = y_val

model.fit(X_train,
          y_train,
          batch_size=32,
          epochs=10,
          validation_data=(X_test, y_test),
          verbose=1)


def load_image(img_path, show=False):
    img = image.load_img(img_path, target_size=(32, 32))
    img_tensor = image.img_to_array(img)  # (height, width, channels)
    img_tensor = np.expand_dims(img_tensor,
                                axis=0)  # (1, height, width, channels), add a dimension because the model expects this shape: (batch_size, height, width, channels)
    img_tensor /= 255.  # imshow expects values in the range [0, 1]

    if show:
        from networkx.drawing.tests.test_pylab import plt
        plt.imshow(img_tensor[0])
        plt.axis('off')
        plt.show()

    return img_tensor


# img_path = 'C:/Users/Ferhat/Python Code/Workshop/Tensoorflow transfer learning/blue_tit.jpg'
img_path = '../Dataset/Test/00000.png'
new_image = load_image(img_path)

pred = model.predict(new_image)

print(pred)
