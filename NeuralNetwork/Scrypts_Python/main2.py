import inter as inter
from tensorflow.keras.models import load_model
from skimage import transform
from skimage import exposure
from skimage import io
from imutils import paths
import numpy as np
import argparse
import imutils
import random
import cv2
import os
import matplotlib.pyplot as plt

model = load_model('../SavedModels/TraficSignNet.h5')

classes = ['Begin of a speed limit', 'End of a speed limit',
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

from PIL import Image
import glob

image_list = []
i = 0
for filename in glob.glob('/Users/botondhalasz/Desktop/Allamvizsga/NeuralNetwork/Dataset/Test/*.png'):
    i += 1
    if i < 25:
        im = cv2.imread(filename)
        im = cv2.resize(im, (32, 32), interpolation=cv2.INTER_AREA)
        image_list.append(im)
    else:
        break


# prediction = model.predict(image_list)
# print(prediction)

# # initialise GUI
# top = tk.Tk()
# top.geometry('800x600')
# top.title('Traffic sign classification')
# top.configure(background='#CDCDCD')
#
# label = Label(top, background='#CDCDCD', font=('arial', 15, 'bold'))
# sign_image = Label(top)


def classify(image):
    # image = cv2.resize(image, (32, 32), cv2.INTER_AREA)
    image = exposure.equalize_adapthist(image, clip_limit=0.1)

    image = image.astype("float32") / 255.0
    image = np.expand_dims(image, axis=0)

    preds = model.predict(image)
    j = preds.argmax(axis=1)[0]
    label = classes[j]

    #image = cv2.resize(image, (128,128), interpolation=cv2.INTER_AREA)
    cv2.putText(image, label, (5, 15), cv2.FONT_HERSHEY_SIMPLEX,
                0.45, (0, 0, 255), 2)
    cv2.imshow(classes[j], image)
    sign = classes[j]
    print(sign)
    print(preds)
    cv2.waitKey()



for i in range(0, 10):
    classify(image_list[i])

# def show_classify_button(file_path):
#     classify_b = Button(top, text="Classify Image", command=lambda: classify(file_path), padx=10, pady=5)
#     classify_b.configure(background='#364156', foreground='white', font=('arial', 10, 'bold'))
#     classify_b.place(relx=0.79, rely=0.46)
#
#
# def upload_image():
#     try:
#         file_path = filedialog.askopenfilename()
#         uploaded = Image.open(file_path)
#         uploaded.thumbnail(((top.winfo_width() / 2.25), (top.winfo_height() / 2.25)))
#         im = ImageTk.PhotoImage(uploaded)
#
#         sign_image.configure(image=im)
#         sign_image.image = im
#         label.configure(text='')
#         show_classify_button(file_path)
#     except:
#         pass
#
#
# upload = Button(top, text="Upload an image", command=upload_image, padx=10, pady=5)
# upload.configure(background='#364156', foreground='white', font=('arial', 10, 'bold'))
#
# upload.pack(side=BOTTOM, pady=50)
# sign_image.pack(side=BOTTOM, expand=True)
# label.pack(side=BOTTOM, expand=True)
# heading = Label(top, text="Know Your Traffic Sign", pady=20, font=('arial', 20, 'bold'))
# heading.configure(background='#CDCDCD', foreground='#364156')
# heading.pack()
# top.mainloop()
