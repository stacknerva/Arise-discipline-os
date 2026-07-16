import wave, struct, math

sampleRate = 44100.0 # hertz
duration = 1.0 # seconds
frequency = 440.0 # hertz

obj = wave.open('app/src/main/res/raw/arise_notification_trimmed.wav','w')
obj.setnchannels(1) # mono
obj.setsampwidth(2)
obj.setframerate(sampleRate)

for i in range(int(duration * sampleRate)):
    value = int(32767.0*math.cos(frequency*math.pi*float(i)/float(sampleRate)))
    data = struct.pack('<h', value)
    obj.writeframesraw( data )

obj.close()
