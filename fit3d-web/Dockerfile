FROM tomcat:7.0.84-jre8
MAINTAINER Florian Kaiser <contact@fkaiser.bio>

COPY setenv.sh /usr/local/tomcat/bin/setenv.sh

RUN apt-get update \
    && apt-get install -y \
       r-base
RUN R -e 'install.packages("sfsmisc", repos="http://cran.rstudio.com/")'

COPY tomcat-users.xml /usr/local/tomcat/conf/
RUN chmod 600 /usr/local/tomcat/conf/tomcat-users.xml

RUN rm -r /usr/local/tomcat/webapps/

COPY target/fit3d-web-0.1.0.war /usr/local/tomcat/webapps/fit3d.war

CMD ["catalina.sh","run"]
