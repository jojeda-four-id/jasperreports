/*
 * JasperReports - Free Java Reporting Library.
 * Copyright (C) 2001 - 2014 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of JasperReports.
 *
 * JasperReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JasperReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.jasperreports.components.barcode4j;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRComponentElement;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.Renderable;
import net.sf.jasperreports.engine.RenderableUtil;
import net.sf.jasperreports.engine.type.ImageTypeEnum;
import net.sf.jasperreports.engine.type.OnErrorTypeEnum;
import net.sf.jasperreports.engine.util.JRColorUtil;

import com.itextpdf.text.pdf.qrcode.ByteMatrix;
import com.itextpdf.text.pdf.qrcode.EncodeHintType;
import com.itextpdf.text.pdf.qrcode.QRCodeWriter;
import com.itextpdf.text.pdf.qrcode.WriterException;

/**
 * 
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 */
public class QRCodeRasterizedImageProducer implements QRCodeImageProducer
{
	
	public Renderable createImage(
		JasperReportsContext jasperReportsContext,
		JRComponentElement componentElement, 
		QRCodeBean qrCodeBean, 
		String message
		)
	{
		QRCodeWriter writer = new QRCodeWriter();

		Map<EncodeHintType,Object> hints = new HashMap<EncodeHintType,Object>();
		hints.put(EncodeHintType.CHARACTER_SET, QRCodeComponent.PROPERTY_DEFAULT_ENCODING);
		hints.put(EncodeHintType.ERROR_CORRECTION, qrCodeBean.getErrorCorrectionLevel().getErrorCorrectionLevel());

		int resolution = JRPropertiesUtil.getInstance(jasperReportsContext).getIntegerProperty(
				componentElement, BarcodeRasterizedImageProducer.PROPERTY_RESOLUTION, 300);
		int margin = qrCodeBean.getMargin() == null ? 0 : qrCodeBean.getMargin();
		try
		{
			ByteMatrix matrix = 
				writer.encode(
					message, 
//					(int)((72f / 2.54f) * componentElement.getWidth()), 
//					(int)((72f / 2.54f) * componentElement.getHeight()), 
					(int)((resolution / 72f) * (componentElement.getWidth() - margin)), 
					(int)((resolution / 72f) * (componentElement.getHeight() - margin)), 
					hints
					);
			BufferedImage image = getImage(matrix, componentElement.getForecolor(), componentElement.getBackcolor());
			return RenderableUtil.getInstance(jasperReportsContext).getRenderable(image, ImageTypeEnum.PNG, OnErrorTypeEnum.ERROR);
		}
		catch (WriterException e)
		{
			throw new JRRuntimeException(e);
		}
		catch (JRException e)
		{
			throw new JRRuntimeException(e);
		}
	}
	
	public BufferedImage getImage(ByteMatrix matrix, Color onColor, Color offColor) 
	{
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		int type = Color.BLACK.equals(onColor) && Color.WHITE.equals(offColor) 
				|| Color.WHITE.equals(onColor) && Color.BLACK.equals(offColor)
				? BufferedImage.TYPE_BYTE_BINARY
				: BufferedImage.TYPE_INT_RGB;
		BufferedImage image = new BufferedImage(width, height, type);
		int onArgb = JRColorUtil.getOpaqueArgb(onColor, Color.BLACK);
		int offArgb = JRColorUtil.getOpaqueArgb(offColor, Color.WHITE);
		for (int x = 0; x < width; x++) 
		{
			for (int y = 0; y < height; y++) 
			{
				image.setRGB(x, y, matrix.get(x, y) > - 1 ? onArgb : offArgb);
			}
		}
		return image;
	}
}
