rm -fR ./huffman huffman.jar && mkdir ./huffman && javac -d ./ ./src/huffman/* && jar -cvfm huffman.jar manifest.txt ./huffman/*
