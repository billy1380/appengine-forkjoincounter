# appengine-forkjoincounter
Fork join counter for appengine based on Brett Slatkin's Google IO 2010 video found at https://www.youtube.com/watch?v=zSDC_TU7rtc

Right now the counters seems to either over or under count. Last thing I tried to do to stablise it was to add a running total of the counter in memcache, which helps a little but my guess is that there is something not quite right with my interpretation of what the behaviour should be.

I have put the original code in python here in at gist https://gist.github.com/billy1380/7500cfa630d4a32825ae so if anyone is interested in taking a look please feel free to do so.
