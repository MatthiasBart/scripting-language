
default: build interpret

interpret: 
	- docker run interpreter

build: 
	- docker build --tag interpreter . 
