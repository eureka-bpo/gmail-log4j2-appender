def buildLog = new File( basedir, 'build.log' )
assert buildLog.text.contains('event message successfully sent')
