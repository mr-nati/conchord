import time
import ntplib
from time import ctime


print 'epoch time: ', int(time.time())


c = ntplib.NTPClient()
response = c.request('pool.ntp.org')
print 'ctime: ', ctime(response.tx_time)