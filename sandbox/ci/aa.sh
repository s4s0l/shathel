#Przerobić ten obraz tak aby miał Docker in Docker
#https://hub.docker.com/r/foxylion/jenkins-slave/ (pozatym ma fleka bo przed chwilą
# próbowałem, ale poprawiłem i śmiga.
# W jenkinsie użyć plugina https://wiki.jenkins-ci.org/display/JENKINS/Slave+Setup+Plugin
# który wystartuje efemerycznego slava w dockerze (może w klastrze rancher/swarm whatever)
# użyć docker workflow plugin do startu albo builda w kolejnym dockerze albo waszego
# środowiska, np composem
# mnie najlepiej sie sprawdza posiadanie builda ktory startuje dockera do którego montuje
# zródła, używam do tego czegoś w rodzaju https://github.com/s4s0l/dind4j. Ten dind ma to do siebie
# że można w nim odpalać kolejne dockery (to nam da już 3 zagnieżdżenie) ale dzięki temu
# ten build jest powtarzalny nie ważne czy jest uruchamiany na CI czy lokalnie (przynajmniej na linuxie,
# ale na windowsie też się uda jak otoczy się mikro skryptem)
#+
#https://dzone.com/refcardz/continuous-delivery-with-jenkins-workflow <- section 10 docker workflow plugin

set -e
docker rm -f -v jenkins | true
docker rm -f -v jenkins-slave | true
rm -r /data/deleteme_jenkins
rm -r /data/deleteme_jenkins_slave
mkdir -p /data/deleteme_jenkins
chmod a+rw /data/deleteme_jenkins
mkdir -p /data/deleteme_jenkins_slave
chmod a+rw /data/deleteme_jenkins_slave


docker run -d --name jenkins -p 8080:8080 -p 50000:50000 \
           -v /data/deleteme_jenkins:/var/jenkins_home \
           foxylion/jenkins

while true; do
  curl http://localhost:8080 && break
  sleep 1
done
docker run -d --restart=always --name jenkins-slave \
	           -v /data/deleteme_jenkins_slave:/home/jenkins \
	           -v /var/run/docker.sock:/var/run/docker.sock \
	           --link jenkins:jenkins \
	           -e JENKINS_URL=http://jenkins:8080 \
	           foxylion/jenkins-slavex