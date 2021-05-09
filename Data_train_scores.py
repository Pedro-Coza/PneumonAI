#!/usr/bin/env python
# coding: utf-8

# Vamos a entrenar el modelo de la pagina web con un data set aumentado, utilizando data generator, y el validation set como validation set

# In[1]:


from keras.preprocessing.image import ImageDataGenerator

# data generator for training
train_datagen = ImageDataGenerator(
        rescale=1./255,
        shear_range=0.3,
        zoom_range=0.2,
        vertical_flip=True)

train_generator = train_datagen.flow_from_directory(
        "C:/Users/leon-/OneDrive/Escritorio/ML/RADIOGRAFIAS/chest_xray/train",
        target_size=(150, 150),
        batch_size=32,
        class_mode='binary',
shuffle=True)

# data generator for validation
validation_datagen = ImageDataGenerator(rescale=1./255,)
validation_generator = validation_datagen.flow_from_directory(
        "C:/Users/leon-/OneDrive/Escritorio/ML/RADIOGRAFIAS/chest_xray/val",
        target_size=(150, 150),
        batch_size=32,
        class_mode='binary',
shuffle=True)

# data generator for testing
test_datagen = ImageDataGenerator(rescale=1./255)
test_generator = test_datagen.flow_from_directory(
        "C:/Users/leon-/OneDrive/Escritorio/ML/RADIOGRAFIAS/chest_xray/test",
        target_size=(150, 150),
        batch_size=32,
        class_mode='binary',
shuffle=True)


# In[2]:


# General libraries
import os
import numpy as np
import pandas as pd 
import random
import cv2
import matplotlib.pyplot as plt
get_ipython().run_line_magic('matplotlib', 'inline')

# Deep learning libraries
import keras.backend as K
from keras.models import Model, Sequential
from keras.layers import Input, Dense, Flatten, Dropout, BatchNormalization
from keras.layers import Conv2D, SeparableConv2D, MaxPool2D, LeakyReLU, Activation
from keras.optimizers import Adam
from keras.preprocessing.image import ImageDataGenerator
from keras.callbacks import ModelCheckpoint, ReduceLROnPlateau, EarlyStopping
import tensorflow as tf
# Input layer
inputs = Input(shape=(150, 150, 3))

# First conv block
x = Conv2D(filters=16, kernel_size=(3, 3), activation='relu', padding='same')(inputs)
x = Conv2D(filters=16, kernel_size=(3, 3), activation='relu', padding='same')(x)
x = MaxPool2D(pool_size=(2, 2))(x)

# Second conv block
x = SeparableConv2D(filters=32, kernel_size=(3, 3), activation='relu', padding='same')(x)
x = SeparableConv2D(filters=32, kernel_size=(3, 3), activation='relu', padding='same')(x)
x = BatchNormalization()(x)
x = MaxPool2D(pool_size=(2, 2))(x)

# Third conv block
x = SeparableConv2D(filters=64, kernel_size=(3, 3), activation='relu', padding='same')(x)
x = SeparableConv2D(filters=64, kernel_size=(3, 3), activation='relu', padding='same')(x)
x = BatchNormalization()(x)
x = MaxPool2D(pool_size=(2, 2))(x)

# Fourth conv block
x = SeparableConv2D(filters=128, kernel_size=(3, 3), activation='relu', padding='same')(x)
x = SeparableConv2D(filters=128, kernel_size=(3, 3), activation='relu', padding='same')(x)
x = BatchNormalization()(x)
x = MaxPool2D(pool_size=(2, 2))(x)
x = Dropout(rate=0.2)(x)

# Fifth conv block
x = SeparableConv2D(filters=256, kernel_size=(3, 3), activation='relu', padding='same')(x)
x = SeparableConv2D(filters=256, kernel_size=(3, 3), activation='relu', padding='same')(x)
x = BatchNormalization()(x)
x = MaxPool2D(pool_size=(2, 2))(x)
x = Dropout(rate=0.2)(x)

# FC layer
x = Flatten()(x)
x = Dense(units=512, activation='relu')(x)
x = Dropout(rate=0.7)(x)
x = Dense(units=128, activation='relu')(x)
x = Dropout(rate=0.5)(x)
x = Dense(units=64, activation='relu')(x)
x = Dropout(rate=0.3)(x)

# Output layer
output = Dense(units=1, activation='sigmoid')(x)

# Creating model and compiling
model = Model(inputs=inputs, outputs=output)
model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])

# Callbacks
checkpoint = ModelCheckpoint(filepath='MOBILNET/pesos.hdf5', save_best_only=True, save_weights_only=True)
lr_reduce = ReduceLROnPlateau(monitor='val_loss', factor=0.3, patience=2, verbose=2, mode='max')
#early_stop = EarlyStopping(monitor='val_loss', min_delta=0.1, patience=1, mode='min')


# In[3]:


import keras
hist = model.fit_generator(
           train_generator,  
           epochs=30, validation_data=validation_generator, 
            callbacks=[checkpoint, lr_reduce])


# In[4]:


import matplotlib.pyplot as plt
#accuracy
plt.plot(hist.history['accuracy'])
plt.plot(hist.history['val_accuracy'])
plt.title('model accuracy')
plt.ylabel('accuracy')
plt.xlabel('epoch')
plt.legend(['train', 'test'], loc='upper left')
plt.savefig('C:/Users/leon-/OneDrive/Escritorio/accuracy_MOBILNET.png')
plt.show()

#loss
plt.plot(hist.history['loss'])
plt.plot(hist.history['val_loss'])
plt.title('model loss')
plt.ylabel('loss')
plt.xlabel('epoch')
plt.legend(['train', 'test'], loc='upper left')
plt.savefig('C:/Users/leon-/OneDrive/Escritorio/loss_MOBILNET.png')
plt.show()


# In[5]:


##Valoracion del modelo

#Cargamos los datos en memoria

import os
import cv2
import numpy as np
import tensorflow as tf
import matplotlib.pyplot as plt
##CARGAMOS LOS DATOS DE TRAIN Y CREAMOS LOS VECTORES
training_data=[]
DATADIR= "C:/Users/leon-/OneDrive/Escritorio/ML/RADIOGRAFIAS/chest_xray/train"
CATEGORIES=["NORMAL","PNEUMONIA"]
def create_training_data():
    for category in CATEGORIES:
        path=os.path.join(DATADIR,category) #path to mama o papa
        class_num=CATEGORIES.index(category)
        for img in os.listdir(path):
            img_array=cv2.imread(os.path.join(path,img))
            new_array=cv2.resize(img_array,(150,150))
            training_data.append([new_array,class_num])
create_training_data()       
import numpy as np
import random
import pandas as pd
random.shuffle(training_data)
X_train=[]
Y_train=[]
for features,label in training_data:
    X_train.append(features)
    Y_train.append(label)
##LO CONVERTIMOS EN NUMPY ARRAYS Y LO NORMALIZAMOS    
X_train_v=np.array(X_train)
Y_train_v=np.array(Y_train)

X_train_v=X_train_v/255

del training_data, X_train,Y_train   #VAMOS BORRANDO VARIABLES PARA NO SATURAR LA RAM 

##CREAMOS LOS DATOS DE TEST Y LOS CARGAMOS EN LAS VARIABLES
testing_data=[]
DATADIR= "C:/Users/leon-/OneDrive/Escritorio/ML/RADIOGRAFIAS/chest_xray/test"
CATEGORIES=["NORMAL","PNEUMONIA"]
import matplotlib.pyplot as plt
def create_testing_data():
    for category in CATEGORIES:
        path=os.path.join(DATADIR,category) #path to mama o papa
        class_num=CATEGORIES.index(category)
        for img in os.listdir(path):
            img_array=cv2.imread(os.path.join(path,img))
            new_array=cv2.resize(img_array,(150,150))
            testing_data.append([new_array,class_num])
create_testing_data()  

random.shuffle(testing_data)
X_test=[]
Y_test=[]
for features,label in testing_data:
    X_test.append(features)
    Y_test.append(label)
    
X_test_v=np.array(X_test)
Y_test_v=np.array(Y_test)
del testing_data, X_test,Y_test    
    


# In[6]:


##Analizamos las metricas tanto en el train set como en el test set
from sklearn.metrics import confusion_matrix
from sklearn.metrics import precision_score, recall_score 
from keras.models import load_model
y_pred_train=np.round(model.predict(X_train_v))
y_pred_test=np.round(model.predict((X_test_v/255)))
print(confusion_matrix(Y_train_v,y_pred_train))
print("precision train: ", precision_score(Y_train_v,y_pred_train))
print("recall train: ", recall_score(Y_train_v,y_pred_train))
print(confusion_matrix(Y_test_v,y_pred_test))
print("precision test: ", precision_score(Y_test_v,y_pred_test))
print("recall test: ", recall_score(Y_test_v,y_pred_test))


# In[7]:


import keras
keras.models.save_model(model,'MOBILNET/model1.h5')

    
