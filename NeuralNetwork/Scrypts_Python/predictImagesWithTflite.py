import PIL
from PIL.Image import Image
from math import exp

classes = ['Begin of a speed limit (20km/h)',
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

from keras.models import load_model
import cv2
import numpy as np
import tensorflow as tf


def softmax(vector):
    sum = 0.0
    for el in vector:
        sum += exp(el)
    newVector = []
    k=0
    for i in vector:
        newVector.append(exp(i) / sum )
    return newVector


interpreter = tf.lite.Interpreter(
    model_path="FirstModel200EPIL.tflite")
interpreter.allocate_tensors()

input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

#basePAth = '/Users/botondhalasz/Desktop/Allamvizsga/NeuralNetwork/Scrypts_Python/TestMicro/'
basePAth = '/Users/botondhalasz/Desktop/Allamvizsga/NeuralNetwork/Test/'

from csv import reader

with open(basePAth + 'Test.csv', 'r') as read_obj:
    input_shape = input_details[0]['shape']
    print(input_shape)
    csv_reader = reader(read_obj)
    sum = 0
    i = 0
    megtalalasiAtlag = 0.0
    for row in csv_reader:
        i += 1
        # microTest
        # classId = row[6]
        # imPath = row[7]
        # big Test
        classId = row[0]
        imPath = row[1]

        # cv2 and clahe, rosszabb megoldas
        # img = cv2.imread(basePAth + imPath)
        # image = cv2.cvtColor(img, cv2.COLOR_RGB2LAB)
        # lab_planes = cv2.split(image)
        # clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
        # lab_planes[0] = clahe.apply(lab_planes[0])
        # image = cv2.merge(lab_planes)
        # bgr = cv2.cvtColor(image, cv2.COLOR_LAB2RGB)
        # image_fromarray = PIL.Image.fromarray(img, 'RGB')
        # img = cv2.resize(img, (input_shape[1], input_shape[2]))
        # img = np.expand_dims(img, axis=0)

        # read image with cv2
        img = cv2.imread(basePAth + imPath)
        img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        image_fromarray = PIL.Image.fromarray(img, 'RGB')
        img = cv2.resize(img, (input_shape[1], input_shape[2]))
        img = np.expand_dims(img, axis=0)

        # read image with pil
        # img = PIL.Image.open(basePAth + imPath)
        # resized_image = img.resize((input_shape[1], input_shape[2]))
        # img = np.expand_dims(resized_image, axis=0)
        #img = np.reshape(img, input_shape)

        input_data = np.array(img, dtype=np.float32)

        interpreter.set_tensor(input_details[0]['index'], input_data)
        interpreter.invoke()

        output_data = interpreter.get_tensor(output_details[0]['index'])

        output_vector = output_data[0]
        result = softmax(output_vector)



        j = output_data.argmax(axis=1)[0]

        if j == int(classId):
            sum += 1
        else:
            print('Eredeti: ', classId, ' -- ',result[int(classId)]*1000 , ' | ', 'Megtalalt: ', j, ' -- ', result[j]*1000)
        print("Helyesen megtalalta: " , j, ' -- ', result[j]*1000)
    print('Osszes megtalalt {0} kepbol:  '.format(i), sum)
    print('Szazalek: ', sum / i)
    print('Megtalalasi atlag:', megtalalasiAtlag / i)

# input_shape = input_details[0]['shape']
# input_data =np.array(img, dtype=np.float32)
# interpreter.set_tensor(input_details[0]['index'], input_data)
# interpreter.invoke()
#
# output_data = interpreter.get_tensor(output_details[0]['index'])
# j = output_data.argmax(axis=1)[0]
# print(output_data, j)

# basePAth = '/Users/botondhalasz/Desktop/Allamvizsga/NeuralNetwork/Scrypts_Python/TestMicro/'
#
# from csv import reader
#
# with open(basePAth + 'Test.csv', 'r') as read_obj:
#     csv_reader = reader(read_obj)
#     sum = 0
#     for row in csv_reader:
#         classId = row[6]
#         imPath = row[7]
#         img = cv2.imread(basePAth + imPath)
#         img = cv2.resize(img, (30, 30))
#         img = np.reshape(img, [1, 30, 30, 3])
#         pred = model.predict(img)
#         j = pred.argmax(axis=1)[0]
#
#         if j == int(classId):
#             sum += 1
#         else:
#             print('Eredeti: ', classId, '  ', 'Megtalalt: ', j)
#
#     print('Osszes megtalalt 200 kepbol:  ', sum)
