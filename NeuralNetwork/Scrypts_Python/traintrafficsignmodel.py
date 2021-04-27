# set the matplotlib backend so figures can be saved in the background
import matplotlib

matplotlib.use("Agg")
# import the necessary packages
# from pyimagesearch.trafficsignmodel import TrafficSignNet
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.optimizers import Adam
from tensorflow.keras.utils import to_categorical
from sklearn.metrics import classification_report
from skimage import transform
from skimage import exposure
from skimage import io
import matplotlib.pyplot as plt
import numpy as np
import argparse
import random
import os


def load_split(classes):
    # initialize the list of data and labels
    data = []
    labels = []

    for i in range(classes):
        path = "/Users/botondhalasz/Desktop/Allamvizsga/NeuralNetwork/Dataset/Train/{0}/".format(i)
        # path = "/Users/botondhalasz/venv/lib/python3.8/site-packages/MyProject/Train/{0}/".format(i)
        print(path)
        Class = os.listdir(path)
        for a in Class:
            try:
                image = io.imread(path + a)
                image = transform.resize(image, (32, 32))
                image = exposure.equalize_adapthist(image, clip_limit=0.1)
                data.append(image)
                labels.append(i)
            except AttributeError:
                print(" ")
    # convert the data and labels to NumPy arrays
    data = np.array(data)
    labels = np.array(labels)
    # return a tuple of the data and labels
    return (data, labels)


# construct the argument parser and parse the arguments
# ap = argparse.ArgumentParser()
# ap.add_argument("-d", "--dataset", required=True,
#                 help="path to input GTSRB")
# ap.add_argument("-m", "--model", required=True,
#                 help="path to output model")
# ap.add_argument("-p", "--plot", type=str, default="plot.png",
#                 help="path to training history plot")
# args = vars(ap.parse_args())

NUM_EPOCHS = 30
INIT_LR = 1e-3
BS = 64

channels = 10
classes = 36