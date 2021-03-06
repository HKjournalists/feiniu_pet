package com.lvmama.order.util;

import java.util.Date;

import com.lvmama.comm.utils.DateUtil;

public class GeneralSequenceNo {

	private static short counter = (int) 1;
	
	protected static short getCount() 
	{
		synchronized (GeneralSequenceNo.class) 
		{
			if (counter < 0)
				counter = 1;
			return counter++;
		}
	}
	
	private static String format(int intval) 
	{
		String formatted = Integer.toString(intval);
		StringBuffer buf = new StringBuffer("000000");
		buf.replace(6 - formatted.length(), 6, formatted);
		return buf.toString();
	}
	
	/**
	 * 生成6位字串，步长为1自动增长
	 */
	public static String generate() 
	{
		return new StringBuffer(36).append(format(getCount())).toString();
	}
	
	/**
	 * 生成批次号
	 * 
	 * @return 批次号
	 */
	public static String generateSerialNo() {
		StringBuffer sb = new StringBuffer();
		sb.append(DateUtil.getFormatDate(new Date(), "yyyyMMddHHmmss"));
		sb.append(generate());

		return sb.toString();
	}
	
	public static void main(String args[])
	{
		for(int i = 0; i < 100; i++)
		{
			System.out.println(generate());
		}
	}
}
