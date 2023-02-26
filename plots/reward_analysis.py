
import math
import os
from matplotlib import pyplot as plt
from matplotlib.animation import FuncAnimation
import matplotlib.animation as animation
import numpy as np

#define the number of plots

fig, axes = plt.subplots(2,2)

concept_x = []
concept_y = []
conceptLevelA_x = []
conceptLevelA_y = []
conceptLevelD_x = []
conceptLevelD_y = []
probabilities = []
concept_levels = []
neural_data = []

countConcept = 0

 

#next we need to see which concept levels are being activated     

def processActivatedNeurons(log):

    temp = log.split(" ")

    tempnew = temp[0]

    if len(temp) > 3:

        for i in range(3,len(temp)):

            conceptLevelA_x.append(tempnew)

        

    conceptLevelA_x.append(tempnew)
    key = temp[2]
    #print("key = ", key[7:])
    #extract the keys
    conceptLevelA_y.append(key)

    plt.subplot(2,2,1)
    plt.xlabel("time")
    plt.ylabel("reward id")
    axes[0,1].set_title("Activation plot")
    plt.tick_params(axis='x',which='both',bottom=False,top=False)
    axes[0,1].scatter(conceptLevelA_x, conceptLevelA_y,sizes=[2]*len(conceptLevelA_x))
    plt.autoscale()

def processDeactivatedNeurons(log):

    #print("deactivate log = "+log)
    temp = log.split(" ")
    tempnew = temp[0]

    if len(temp) > 3:

        for i in range(4,len(temp)):

            conceptLevelD_x.append(tempnew)

        

    conceptLevelD_x.append(tempnew)

            

    keys = temp[7:]

    #extract the keys

    for key in keys:

        print("key = " +key)

        if key != " " and key != "":

            conceptLevelD_y.append(concept_level(key))

    sizeArray = [1]*len(conceptLevelD_x)

    plt.xlabel("time")

    plt.ylabel("concept level")

    axes[1,1].set_title("Deactivation graph")

    axes[1,1].sharex(axes[0, 0])

    axes[1,1].scatter(conceptLevelD_x, conceptLevelD_y, sizes=[2]*len(conceptLevelD_x))

def process_probMap(log):
    #plot a contour plot of probabilities and concept level

    temp = log.split(" ")
    #print("temp = "+temp)
    probabilities.append(temp[8])
    concept_levels.append(concept_level(temp[5]))

    axes[1,0].set_title("Probability map")
    plt.xlabel("Probabilities")
    plt.ylabel("Concept levels")
    #plt.gray()
    axes[1,0].scatter(probabilities, concept_levels, c = "blue", s=1)


def animate_conceptGraph(i):

    log = neural_data[i]

    log = log.replace(":","")

    

    log = neural_data[i]

    log = log.replace("\n","")

    log = log.replace("(", " ")

    log = log.replace(")", " ")

    log = log.replace("[", "")

    log = log.replace("]", "")

    log = log.replace(":", "")

    log = log.replace("  ", " ")

    tempnew = ""

    temp = []

    print("i = " + str(i))
    
    if log.find("Activated ") != -1:

        processActivatedNeurons(log)

        

    elif log.find("Deactivated ") != -1:

        processDeactivatedNeurons(log)

    elif log.find("interested calculated for ") != -1:
        process_probMap(log)


    if i % 100 ==0:
        plt.savefig("/home/abhishek/IdeaProjects/nelson2.0/plots/after optimization/r"+str(i/100)+".png")
        print("Figure saved")
    plt.autoscale()

    

def plot_figs(i):

    animate_conceptGraph(i)


def main():

    global neural_data

    cnt = 0

    while(cnt < 1):

        plt.cla()

        cnt = cnt +1

        FileData = open("/home/abhishek/IdeaProjects/nelson2.0/log-data/log-personality.txt","r")

        neural_data = FileData.readlines()

        FileData.close()

        if len(neural_data) != 0:

            animation_conceptLevel = FuncAnimation(plt.gcf(),plot_figs,interval=0)

            plt.show()

              #print("erwqqg")   
    #animate_images()

    
if __name__ == "__main__":

    main()