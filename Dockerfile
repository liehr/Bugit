FROM ubuntu:latest
LABEL authors="liehr"

ENTRYPOINT ["top", "-b"]