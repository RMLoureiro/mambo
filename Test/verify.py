import numpy as np
import networkx as nx
import csv
import datetime
import copy
import re
import sys

from os import listdir
from os.path import isfile, join



def openFiles():
    return [f for f in listdir(mypath) if isfile(join(mypath, f))]



def logsToData():
    global actives
    global logs
    global fullData
    for i in range(0, nodes):
        file = open(sys.argv[1] + logs[i], "r")
        viewsFile = open(file.name[:-4] + "-views.csv" , "x")
        logedFile = open(file.name[:-4] + "-LOGGED.csv" , "x")
        
        lines = file.readlines()
        for line in lines:
            if line[:5] == "LOGS,":
                viewsFile.write(line[5:])
            if line[:4] == "LOGS":
                logedFile.write(line[5:])
                
        viewsFile.close()
        logedFile.close()
        viewsFile = open(file.name[:-4] + "-views.csv" , "r")

        data = list(csv.reader(viewsFile, delimiter = ','))
        
        actives[i] = data[-1]
        fullData[i] = data
        
        logs[i] = logs[i].replace('.csv', '')
        
        for j in range(1, size+1): 
            node = actives[i][j]
            node = node.replace('/', '').replace(' ','')
            if not node == str(-1):
                graph.add_edge(logs[i], node)
           
        file.close()
        
    actives = np.array(actives)
    
    global nedges
    nedges = len(graph.edges)
    
    global edges
    edges = graph.edges
    edges = list(edges)
    


def concistency():
    global graph, actives, logs, nodes, size
    for i in range(0, nedges):
        other = edges[i]
        if not( graph.has_edge(other[1], other[0])):
            print('inconsistent ' + str(other[1]) + ' ' + str(other[0]))
            
    
    graph = graph.to_undirected(reciprocal=True)
    print("Clustering Coeficient: ", nx.average_clustering(graph, graph.nodes, 1))
    print("Average Shortest Path: ", nx.average_shortest_path_length(graph))
    print("Raidus (minimum eccentricity): ", nx.radius(graph))
    print("Diameter (maximum eccentricity): ", nx.diameter(graph))
    print("Average node conectvity: ", nx.average_node_connectivity(graph))
    print("Node conectivity: ", nx.node_connectivity(graph))
    print("Isolated nodes: ", *nx.isolates(graph))
    #nx.draw_kamada_kawai(graph)
    
def timeToConverge():
    convergenceTimes = [''] * nodes
    trimmedData = copy.deepcopy(fullData)
    
    for i in range(0, nodes):
        for j in range(0, len(trimmedData[i])):
            del trimmedData[i][j][0]
                 
    for i in range(0, nodes):
        for j in range((len(fullData[i]))-1, 2, -1):
            if (trimmedData[i][j] == trimmedData[i][j-1] and trimmedData[i][j] == trimmedData[i][j-2]):
                convergenceTimes[i] = fullData[i][j-1][0]
            else: 
                break
        
        
    if ('' in convergenceTimes): 
        print("Overlay did not converge")
    else:
        for i in range(0, len(convergenceTimes)):
            values = list(map(int, re.findall(r'\d+', convergenceTimes[i])))
            time = datetime.datetime(values[0], values[1], values[2], values[3], values[4], values[5])
            convergenceTimes[i] = time
    
        time = convergenceTimes[0]
        for i in range(1, len(convergenceTimes)):
            if convergenceTimes[i] > time:
                time = convergenceTimes[i]
                
        
        start = list(map(int, re.findall(r'\d+', fullData[nodes-1][0][0])))
        start = datetime.datetime(start[0], start[1], start[2], start[3], start[4], start[5])
        
        print("Convergence Time: ", time - start)
    
    
size = 5
nodes = 11
mypath = sys.argv[1]
nedges = -1
edges = None
graph = nx.DiGraph()
actives = [''] * nodes
fullData = [''] * nodes


logs = openFiles()
logsToData()
concistency()
timeToConverge()