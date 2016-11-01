package com.blemobi.library.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

class WrappedOutputStream extends ServletOutputStream {
	private ByteArrayOutputStream buffer = null;

	public WrappedOutputStream(ByteArrayOutputStream buffer) {
		this.buffer = buffer;
	}

	public void write(int b) throws IOException {
		buffer.write(b);
	}

	public byte[] toByteArray() {
		return buffer.toByteArray();
	}

	public boolean isReady() {
		return true;
	}

	public void setWriteListener(WriteListener writeListener) {
		
	}
}