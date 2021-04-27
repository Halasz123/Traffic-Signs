import tensorflow as tf
from keras.models import load_model

custom_opdef = """name: 'TFLiteAwesomeCustomOp' input_arg:
  { name: 'In' type: DT_FLOAT } output_arg: { name: 'Out' type: DT_FLOAT }
  attr : { name: 'a1' type: 'float'} attr : { name: 'a2' type: 'list(float)'}"""

# Convert the model
model = load_model('/TFlites/FirstModel100E_clahe_8080.h5')
#tf.lite.python.convert.register_custom_opdefs([custom_opdef])
#tf.lite.OpsSet([custom_opdef])
converter = tf.lite.TFLiteConverter.from_keras_model(model)  # path to the SavedModel directory
converter.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_model = converter.convert()

# Save the model.
with open('/Users/botondhalasz/Desktop/Allamvizsga/NeuralNetwork/CLAHE/models/FirstModel100E_clahe_8080.tflite', 'wb') as f:
  f.write(tflite_model)