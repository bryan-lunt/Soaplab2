#  Set environment variable CLASSPATH.
#  Usage: source run/source.me.csh
#
#  You do not need to use this source file if you use 'run/run-*'
#  scripts - those scripts set CLASSPATH for themselves. Use it if you
#  wish to run some clients that do not have their own run script, or
#  if you wish to set some Java properties on the command line
#  (because the run scripts do not support that).
#
#  $Id: source.me.csh,v 1.5 2007/06/19 21:39:26 marsenger Exp $
# ----------------------------------------------------

set PROJECT_HOME=@PROJECT_HOME@
set LIBS_PATH=@PROJECT_DEPS@

setenv CLASSPATH ${LIBS_PATH}
setenv CLASSPATH `echo ${PROJECT_HOME}/build/lib/*.jar | tr ' ' ':'`:${CLASSPATH}
setenv CLASSPATH ${PROJECT_HOME}/build/classes:${CLASSPATH}
setenv CLASSPATH ${PROJECT_HOME}/build/plugins:${CLASSPATH}

