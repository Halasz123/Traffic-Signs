import PIL

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

model = load_model('/Users/botondhalasz/Desktop/Allamvizsga/NeuralNetwork/Kagle_trye/KagleModel200EPIL_aug.h5')

# model.compile(loss='binary_crossentropy',
#               optimizer='rmsprop',
#               metrics=['accuracy'])

basePAth = '/Users/botondhalasz/Desktop/Allamvizsga/NeuralNetwork/Scrypts_Python/TestMicro/'

from csv import reader

with open(basePAth + 'Test.csv', 'r') as read_obj:
    csv_reader = reader(read_obj)
    sum = 0
    for row in csv_reader:
        classId = row[6]
        imPath = row[7]

        # img = cv2.imread(basePAth + imPath)
        # img = cv2.resize(img, (30, 30))
        img = PIL.Image.open(basePAth + imPath)
        resized_image = img.resize((30,30))
        img = np.reshape(resized_image, [1, 30, 30, 3])
        pred = model.predict(img)
        j = pred.argmax(axis=1)[0]

        if j == int(classId):
            sum += 1
        else:
            print('Eredeti: ', classId, '  ', 'Megtalalt: ', j)

    print('Osszes megtalalt 200 kepbol:  ', sum)
