conchord
========

An app that allows for multiple devices to jam together!

Conchord synchronizes multiple devices so that they play different layers to the same song.<!-- The devices communicate with a shared NTP server in order to synchronize their device clocks while using Firebase to pass information relating to the song being played between them.
-->
In practice, I've seen positive results with roughly 70% accuracy, as this is a work in progress.

A Youtube link of conchord in action: http://goo.gl/SeKCH5.  

UPDATES
=======

UPDATE: I've come to a place where I'm unable to move forward with conchord in the way I'd like. The idea of the app is to have multiple devices play the different parts of a song (vocals, percussion, etc) at the same time, obviously.

However, I've been able to unsatisfactorily synchronize 2 (or more) devices to play a single note closely enough in time so that the human ear can't really tell. Although I was using a common NTP server to help synchronize the varying system times on different devices, the fact that I'm unable to predict the request/response latency time makes the NTP timestamps almost worthless by the time they get to each device. I've brought up the issue in a stackexchange post here: http://goo.gl/77Bif3. I'm going to move on to a different project for now as I look around for a high-speed (low latency) means of device-to-device communication. 

EVEN NEWER UPDATE: Perhaps there's some merit in pursuing Bluetooth 4.0?? (Thanks to [Joseph Afework](https://github.com/Commander147))

LATEST UPDATE: Nope. BLuetooth LE still isn't good enough :(
