FROM hseeberger/scala-sbt:8u265_1.4.3_2.13.4 as builder
WORKDIR /usr/src/app
COPY ./ .
RUN sbt universal:packageBin

FROM openjdk:8
WORKDIR /root/
COPY --from=builder /usr/src/app/target/universal/lab-stock-market-0.1.zip .
RUN unzip /root/lab-stock-market-0.1.zip && chmod +x /root/lab-stock-market-0.1/bin/lab-stock-market
ENTRYPOINT ["./lab-stock-market-0.1/bin/lab-stock-market"]
CMD []