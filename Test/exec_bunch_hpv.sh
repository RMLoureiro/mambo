#!/usr/bin/env bash

OUTPUT_DIR=out
CONTACT_PORT=8000
IP=127.0.0.1
N=30
T=30
run_instance() {
  echo -ne "Starting Instance: "$1 "$2" " number: $3 \r"
  java -cp ../target/mambo-1.0-SNAPSHOT-jar-with-dependencies.jar Main "../props/hpv/node$3.properties" &> ../output/out${4}/${IP}-${1}.csv  &
}

rm -r ../output
mkdir ../output
killall java

for ((j=0; j<=10; j++))
  do
    echo "TEST "$j
    mkdir ../output/out${j}
    run_instance $CONTACT_PORT $CONTACT_PORT 0 $j
    sleep 5
    for ((i=1;i<=N;i++))
        do
            sleep .1
            node_port=$((CONTACT_PORT + i))
            run_instance  $node_port $CONTACT_PORT $i $j
        done

        echo -ne '                                                                                                                   \r'
        for ((k=0; k<=T; k++))
          do
              echo -ne $k "/" $T " \r"
              sleep 1
          done

        killall java
        python3 verify.py "../output/out${j}/"

        echo "#############################################################################################################################"
  done
