import cv2
from PIL import Image
import numpy as np

#
# image = Image.open("/Users/botondhalasz/Desktop/Allamvizsga/NeuralNetwork/Scrypts_Python/TestMicro/Test/00000.png")
# input_data = np.array(image)
# img = Image.fromarray(input_data, 'RGB')
# img.show()

image = cv2.imread("/Users/botondhalasz/Desktop/Allamvizsga/NeuralNetwork/Scrypts_Python/TestMicro/Test/00000.png")
image = cv2.cvtColor(image,cv2.COLOR_RGB2BGR)
# image = exposure.equalize_adapthist(image, clip_limit=0.1)
image_from_array = Image.fromarray(image)
img = np.array(image_from_array)
img = Image.fromarray(img, 'RGB')
img.show()
