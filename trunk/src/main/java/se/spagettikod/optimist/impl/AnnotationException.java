/*
 * Copyright (C) 2010 Roland Bali
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package se.spagettikod.optimist.impl;

/**
 * Thrown when annotations are missing where expected or used incorrectly.
 * 
 * @author Roland Bali
 * 
 */
@SuppressWarnings("serial")
public class AnnotationException extends RuntimeException {

	/**
	 * Constructs an <code>AnnotationException</code> with a message.
	 * 
	 * @param message
	 *            error message explaining what caused the exception.
	 */
	public AnnotationException(String message) {
		super(message);
	}

}
