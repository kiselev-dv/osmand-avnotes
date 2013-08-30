osmand-avnotes
==============

Generate avnotes.gpx file with links to *.wav files with audio notes. 
Programm accepts one parameter - path to folder with notes from osmand.

Don't forget to convert .3gp to .wav first.

    ls -1 | grep .3gp$ | xargs -I {f} echo "avconv -i {f} {f}.wav" | bash
  
