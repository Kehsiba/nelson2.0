import math
from matplotlib import pyplot as plt
from matplotlib.animation import FuncAnimation
import numpy as np

#define the number of plots
fig, axes = plt.subplots(2,2)
concept_x = []
concept_y = []

conceptLevelA_x = []
conceptLevelA_y = []

conceptLevelD_x = []
conceptLevelD_y = []

neural_data = []
countConcept = 0
 
#next we need to see which concept levels are being activated
def base32todec(n):
    return int(n,32)

def concept_level(neuron_id):
    log32 = math.log(base32todec(neuron_id),256)
    return log32        


def process_conceptCreation(log):
    global countConcept
    timestamp = log.split(" ")[0]
    countConcept = countConcept+1
    concept_x.append(timestamp)
    concept_y.append(countConcept)
    #refresh concept graph
    plt.cla()
    plt.xlabel("time")
    plt.ylabel("No. of concept neurons")

    #plt.subplot(3,1,1)
    axes[0,0].scatter(concept_x, concept_y,sizes = [1]*len(concept_x))
    axes[0,0].set_title("Concept creation rate")
    plt.autoscale()    

def processActivatedNeurons(log):
    #print("activate log = "+log)
    temp = log.split(" ")
    tempnew = temp[0]
    if len(temp) > 3:
        for i in range(3,len(temp)):
            conceptLevelA_x.append(tempnew)
        
    conceptLevelA_x.append(tempnew)
            
    keys = temp[2:]

    #extract the keys
    for key in keys:
        #print("key = " +key+"concept-level = "+str(concept_level(key)))
        if key != " " and key != "":
            conceptLevelA_y.append(concept_level(key))

    #print("x = "+str(conceptLevelA_x))
    #print("y = "+str(conceptLevelA_y))


    plt.subplot(2,2,1)
    plt.xlabel("time")
    plt.ylabel("concept level")
    axes[0,1].set_title("Activation plot")

    plt.tick_params(axis='x',which='both',bottom=False,top=False)

    axes[0,1].scatter(conceptLevelA_x, conceptLevelA_y,sizes=[2]*len(conceptLevelA_x))
    axes[0,1].sharex(axes[0, 0])

    plt.autoscale()


def processDeactivatedNeurons(log):
    #print("deactivate log = "+log)
    temp = log.split(" ")
    tempnew = temp[0]
    if len(temp) > 3:
        for i in range(4,len(temp)):
            conceptLevelD_x.append(tempnew)
        
    conceptLevelD_x.append(tempnew)
            
    keys = temp[2:]

    #extract the keys
    for key in keys:
        #print("key = " +key)
        if key != " " and key != "":
            conceptLevelD_y.append(concept_level(key))

    sizeArray = [1]*len(conceptLevelD_x)
    plt.xlabel("time")
    plt.ylabel("concept level")
    axes[1,1].set_title("Deactivation graph")
    axes[1,1].sharex(axes[0, 0])
    axes[1,1].scatter(conceptLevelD_x, conceptLevelD_y, sizes=[2]*len(conceptLevelD_x))




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

    if log.find("Concept created") !=-1:
        process_conceptCreation(log)
        
        
    elif log.find("Activated ") != -1:
        processActivatedNeurons(log)
        
    elif log.find("Deactivated ") != -1:
        processDeactivatedNeurons(log)
    
    plt.autoscale()
    

def plot_figs(i):
    animate_conceptGraph(i)

def main():
    global neural_data
    cnt = 0
    while(cnt < 1):
        plt.cla()
        cnt = cnt +1
        FileData = open("/home/abhishek/IdeaProjects/nelson2.0/log-data/log.txt","r")
        neural_data = FileData.readlines()
        FileData.close()
        if len(neural_data) != 0:
            animation_conceptLevel = FuncAnimation(plt.gcf(),plot_figs,interval=1)
            plt.show()
            #print("erwqqg")   
        



if __name__ == "__main__":
    main()