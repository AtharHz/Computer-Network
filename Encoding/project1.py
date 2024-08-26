from signal import signal
import matplotlib.pyplot as plt
import numpy as np

def differentialNRZ(inputSignal):
    t=np.arange(0,10,1)
    outputSignal=[]
    if(inputSignal[0]==0):
        outputSignal.extend(np.full(len(t),0))
    else:
        outputSignal.extend(np.full(len(t),1))
    k=0
    for i in range(len(inputSignal)-1):
        temp=outputSignal[k]
        if(inputSignal[i+1]==0):
            outputSignal.extend(np.full(len(t),temp))
        else:
            outputSignal.extend(np.full(len(t),(abs(temp-1))))
        k=k+10
    return outputSignal

def ASKEncoder(inputSignal):
    t=np.arange(0,2* np.pi,0.1)
    outputSignal=[]
    for i in range(len(inputSignal)):
        if(inputSignal[i]==0):
            yConst=np.full(len(t),0)
            outputSignal.extend(yConst)
        else:
            yCos2=np.cos(t)
            outputSignal.extend(yCos2)
    return outputSignal
        
def bipolar(inputSignal):
    outputSignal=[]
    flag=False
    t=np.arange(0,10,1)
    for i in range(len(inputSignal)):
        if(inputSignal[i]==0):
            outputSignal.extend(np.full(len(t),0))
        else:
            if(flag):
                outputSignal.extend(np.full(len(t),-1))
                flag=False
            else:
                outputSignal.extend(np.full(len(t),1))
                flag=True
    return outputSignal

#we assume that the output signal was zero before beggining
def differentialManchester(inputSignal):
    outputSignal=[]
    rise=False
    t=np.arange(0,10,1)
    for i in range(len(inputSignal)):
        if(inputSignal[i]==0):
            if(rise):
                outputSignal.extend(np.full(len(t),0))
                outputSignal.extend(np.full(len(t),1))
                rise=True
            else:
                outputSignal.extend(np.full(len(t),1))
                outputSignal.extend(np.full(len(t),0))
                rise=False 
        else:
            if(rise):
                outputSignal.extend(np.full(len(t),1))
                outputSignal.extend(np.full(len(t),0))
                rise=False
            else:
                outputSignal.extend(np.full(len(t),0))
                outputSignal.extend(np.full(len(t),1))
                rise=True
    return outputSignal

def unrepeatedBipolar(inputSignal):
    outputSignal=[]
    flag=False
    for i in range(len(inputSignal)):
        if(inputSignal[i]==0):
                outputSignal.append(0)
        else:
            if(flag):
                    outputSignal.append(-1)
                    flag=False
            else:
                    outputSignal.append(1)
                    flag=True
    return outputSignal

def B8ZS(inputSignal):
    outputSignal=[]
    bipolarSignal=unrepeatedBipolar(inputSignal)
    counter=0
    lastOneIndex=-1
    sign=False
    for i in range(len(bipolarSignal)):
        if(bipolarSignal[i]==1):
            counter=0
            lastOneIndex=i
            sign=True
        elif(bipolarSignal[i]==-1):
            counter=0
            lastOneIndex=i
            sign=False
        else:
            counter=counter+1
            if(counter==8):
                bipolarSignal=substitutionB8ZS(bipolarSignal,sign,lastOneIndex)
                counter=0
                lastOneIndex=lastOneIndex+8
    outputSignal=[]
    t=np.arange(0,10,1)
    for i in range(len(bipolarSignal)):
        outputSignal.extend(np.full(len(t),bipolarSignal[i]))
    return outputSignal


def substitutionB8ZS(signal,sign,lastOneIndex):
    if(sign):
        #000+-0-+
        signal[lastOneIndex+4]=1
        signal[lastOneIndex+5]=-1
        signal[lastOneIndex+7]=-1
        signal[lastOneIndex+8]=1
    else:
        #000-+0+-
        signal[lastOneIndex+4]=-1
        signal[lastOneIndex+5]=1
        signal[lastOneIndex+7]=1
        signal[lastOneIndex+8]=-1
    return signal

def HDB3(inputSignal):
    outputSignal=[]
    bipolarSignal=unrepeatedBipolar(inputSignal)
    zeroCounter=0
    lastOneIndex=-1
    sign=False
    oneCounter=0
    for i in range(len(bipolarSignal)):
        if(bipolarSignal[i]==1):
            if(sign):
                bipolarSignal[i]=-1
                sign=False
            else:
                sign=True
            zeroCounter=0
            oneCounter+=1
            lastOneIndex=i
        elif(bipolarSignal[i]==-1):
            if(sign==False):
                bipolarSignal[i]=1
                sign=True
            else:
                sign=False
            zeroCounter=0
            oneCounter+=1
            lastOneIndex=i
        else:
            zeroCounter=zeroCounter+1
            if(zeroCounter==4):
                bipolarSignal,sign=substitutionHDB3(bipolarSignal,sign,lastOneIndex,oneCounter)
                oneCounter=0
                zeroCounter=0
                lastOneIndex=lastOneIndex+4
    outputSignal=[]
    t=np.arange(0,10,1)
    for i in range(len(bipolarSignal)):
        outputSignal.extend(np.full(len(t),bipolarSignal[i]))
    return outputSignal

def substitutionHDB3(signal,sign,lastOneIndex,oneCounter):
    if(oneCounter%2==1):
        if(sign):
            #000+
            signal[lastOneIndex+4]=1
            sign=True
        else:
            #000-
            signal[lastOneIndex+4]=-1
            sign=False
    else:
        if(sign):
            #-00-
            signal[lastOneIndex+1]=-1
            signal[lastOneIndex+4]=-1
            sign=False
        else:
            #+00+
            signal[lastOneIndex+1]=1
            signal[lastOneIndex+4]=1
            sign=True
    return signal,sign



signal=[]

num=int(input("Enter length of sinal: "))
for i in range(0,num):
    ele=int(input())
    signal.append(ele)

#print(signal)
# signal=[1,1,0,0,0,0,0,0,0,0,1,0,0,0,0,1,1,1,0,0] 
# signal=[0,1,1,0,0,0,0,1,1]
# signal=[1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0]

signal1=differentialNRZ(signal)
t=np.arange(0,(len(signal1)/10),0.1)
plt.figure()
plt.plot(t,signal1)
plt.title('Differential NRZ(N1 to N2)')
plt.grid(True)
plt.show()

signal2=ASKEncoder(signal)
t=np.arange(0,(len(signal2)/10),0.1)
plt.figure()
plt.plot(t,signal2)
plt.title('ASK Encoder(N2 to N3)')
plt.grid(True)
plt.show()

signal3=bipolar(signal)
t=np.arange(0,(len(signal3)/10),0.1)
plt.figure()
plt.plot(t,signal3)
plt.title('Bipolar(N3 to N4)')
plt.grid(True)
plt.show()

signal4=differentialManchester(signal)
t=np.arange(0,(len(signal4)/10),0.1)
plt.figure()
plt.plot(t,signal4)
plt.title('Differential Manchester(N4 to N5)')
plt.grid(True)
plt.show()

signal5=B8ZS(signal)
t=np.arange(0,(len(signal5)/10),0.1)
plt.figure()
plt.plot(t,signal5)
plt.title('B8ZS(N3 to N4 extra)')
plt.grid(True)
plt.show()

signal6=HDB3(signal)
t=np.arange(0,(len(signal6)/10),0.1)
plt.figure()
plt.plot(t,signal6)
plt.title('HDB3(N3 to N4 extra)')
plt.grid(True)
plt.show()