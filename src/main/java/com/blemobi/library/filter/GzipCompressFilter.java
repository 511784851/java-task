package com.blemobi.library.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.blemobi.library.consul.BaseService;
import com.google.common.base.Strings;

public class GzipCompressFilter implements Filter {

	private static int willCompressMinLength = getConfigValue("resp_gzip_compress_min_length", 1024);

	public void init(FilterConfig arg0) throws ServletException {

	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletResponse resp = (HttpServletResponse) response;
		HttpServletRequest req = (HttpServletRequest) request;
		if (isGZipEncoding(req)) {
			GzipWrapper wrapper = new GzipWrapper(resp);
			chain.doFilter(request, wrapper);
			byte[] data = null;
			copyHeader(wrapper,resp);
			if (isGzipPermit(wrapper)) {
				data = gzip(wrapper.getResponseData());
				resp.addHeader("Content-Encoding", "gzip");
				resp.setContentLength(data.length);
			}else{
				data = wrapper.getResponseData();
			}
			ServletOutputStream output = response.getOutputStream();
			output.write(data);
			output.flush();
		} else {
			chain.doFilter(request, response);
		}
	}

	private boolean isGzipPermit(GzipWrapper wrapper) throws IOException {
		return isStatusPermit(wrapper) && isProtoOrJson(wrapper) && isLenPermit(wrapper) && disGzip(wrapper);
	}
	
	private boolean isLenPermit(GzipWrapper wrapper) throws IOException {
		byte[] data = wrapper.getResponseData();
		return (data.length >= willCompressMinLength);
	}



	private static boolean isGZipEncoding(HttpServletRequest request) {
		boolean flag = false;
		String encoding = request.getHeader("Accept-Encoding");
		if (encoding.indexOf("gzip") != -1) {
			flag = true;
		}
		return flag;
	}

	private boolean isStatusPermit(HttpServletResponse response) {
		int status = response.getStatus();
		boolean rtn =  (status >= 200 && status < 300);
		return rtn;
	}

	private void copyHeader(HttpServletResponse source, HttpServletResponse target) {
		for(String header :source.getHeaderNames()){
			target.setHeader(header, source.getHeader(header));
		}
	}

	private boolean disGzip(HttpServletResponse resp ) {
		String value = resp.getHeader("Content-Encoding");
		if (Strings.isNullOrEmpty(value)) {
			return true;
		} else {
			return !value.equalsIgnoreCase("application/gzip");
		}
	}
	
	private boolean isProtoOrJson(HttpServletResponse resp ) {
		String contentType = resp.getContentType();
		if (Strings.isNullOrEmpty(contentType)) {
			return false;
		} else {
			if(contentType.equalsIgnoreCase("application/x-protobuf")){
				return true;
			}else if(contentType.equalsIgnoreCase("application/json")){
				return true;
			}else{
				return false;
			}
		}
	}

	private byte[] gzip(byte[] data) {
		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
		GZIPOutputStream output = null;
		try {
			output = new GZIPOutputStream(byteOutput);
			output.write(data);
		} catch (IOException e) {
		} finally {
			try {
				output.close();
			} catch (IOException e) {
			}
		}
		return byteOutput.toByteArray();
	}

	private static int getConfigValue(String key, int defaultValue) {
		String strValue = BaseService.getProperty(key);
		if (Strings.isNullOrEmpty(strValue)) {
			return defaultValue;
		} else {
			try {
				int v = Integer.parseInt(strValue);
				return (v > 0) ? v : defaultValue;
			} catch (Exception e) {
				return defaultValue;
			}
		}
	}

	public void destroy() {
		
	}

}