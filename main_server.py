# Copyright 2015 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# [START gae_flex_quickstart]
import logging
import werkzeug
import flask
import numpy as np
import tensorflow
import keras
import imageio
import time
from multiprocessing.pool import ThreadPool


app = flask.Flask(__name__)


@app.route('/', methods=['GET'])
def hello():
    return 'Hi there we are improving'


@app.route('/', methods=['POST'])
def handle_request():
    result = ""
    imagefile = flask.request.files['image']
    filename = werkzeug.utils.secure_filename(imagefile.filename)
    print("\nReceived image File name : " + imagefile.filename)
    imagefile.save(filename)
    img = imageio.imread(filename)
    if result == "":
        start = time.time()
        np.asarray(img)
        print("------------- VAMOS A HACER RESHAPE -------------")
        print("------------- Resized image: " + str(img.shape) + " -------------\n")
        image_list = []
        image_list.append(img)
        xray_array = np.array(image_list)/255
        print("------------- CARGAMOS MODELOS -------------")
        modelo_radiografia = keras.models.load_model('modelo_exclusion_malas_fotos_15.h5')
        print("------------- VAMOS A HACER LA PREDICCION -------------\n")
        pred = modelo_radiografia.predict(xray_array)
        pred_redondeada = np.argmax(pred)
        print("------------- PREDICCION REALIZADA -------------\n")
        end = time.time()
        totalTime = end - start
        print("Se ha tardado " + str(totalTime) + " segundos en completar la operación\n")
        value=pred[0][pred_redondeada]*100
        if pred_redondeada == 0:
            print("Esto no es una radiografía payaso | " + str(value))
            result = "Esto no es una radiografía payaso | " + str(value)
        if pred_redondeada == 1:
            print("No se aprecian indicios de pneumonía en su radiografía | " + str(value))
            result = " No se aprecian indicios de neumonía en su radiografía | " + str(value) 
        if pred_redondeada== 2:
            print("Se aprecian indicios de pneumonía en su radiografía | " + str(value))
            result = "Se aprecian indicios de neumonía en su radiografía | " + str(value) 
            
        return result
    else:
        return "Coming from else"


@app.errorhandler(500)
def server_error(e):
    logging.exception('An error occurred during a request.')
    return """
    An internal error occurred: <pre>{}</pre>
    See logs for full stacktrace.
    """.format(e), 500


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=8080, debug=True, processes=8)
# [END gae_flex_quickstart]
