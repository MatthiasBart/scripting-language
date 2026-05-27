interpret: build
	- docker run interpreter

build: 
	- docker build --tag interpreter . 
