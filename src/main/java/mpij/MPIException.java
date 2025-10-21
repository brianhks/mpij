// MPIException.java
/*
 Distributed Object Group Metacomputing Architecture
 Copyright Glenn Judd, Brigham Young University	1998
   The authors hereby grant a non-exclusive license to use, copy, and modify
 this software for educational purposes and without fee provided
 that the above copyright notice and the following paragraphs appear in all
 copies. This code may not be redistributed without written permission
 from the authors.
   In no event shall the authors be liable to any party for direct, indirect,
 special incidental, or consequential damage arising out of the use of this
 software even if the authors have been advised of the possibility of such
 damage.
   THE AUTHORS SPECIFICALLY DISCLAIM ANY WARRANTIES INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS" BASIS
 AND THE AUTHORS HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.
*/

package mpij;

/**
 * MPI exception base class.
 * @version	0.8
 * @author	Glenn Judd
 */
public class MPIException extends Exception {
	String		msg;
	Exception	nestedException;

	/**
	 * Construct an MPIException with the given message.
	 */
	public MPIException(String msg) {
		this.msg = msg;
	}


	/**
	 * Construct an MPIException with the given message and nested exception.
	 */
	public MPIException(String msg, Exception nestedException) {
		this.msg = msg;
		this.nestedException = nestedException;
	}


	/**
	 * Display the stack trace of where this exception occurred or
	 * where the nested exception occurred if one exists.
	 */
	public void printStackTrace() {
		System.out.println("MPIException: "+ msg);
		if (nestedException != null) {
			System.out.println("Nested Exception:");
			nestedException.printStackTrace();
		} else {
			super.printStackTrace();
		}
	}
}