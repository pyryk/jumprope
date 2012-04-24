/*
 * Created on Apr 23, 2012
 * @author verkel
 */
package jumprope.tests;

import javax.vecmath.Quat4f;

import processing.core.PMatrix3D;


public class MatrixUtils {
	
	public static void setRotation(PMatrix3D dest, Quat4f q) {
		float d = q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w;
		assert (d != 0f);
		float s = 2f / d;
		float xs = q.x * s, ys = q.y * s, zs = q.z * s;
		float wx = q.w * xs, wy = q.w * ys, wz = q.w * zs;
		float xx = q.x * xs, xy = q.x * ys, xz = q.x * zs;
		float yy = q.y * ys, yz = q.y * zs, zz = q.z * zs;
		dest.m00 = 1f - (yy + zz);
		dest.m01 = xy - wz;
		dest.m02 = xz + wy;
		dest.m10 = xy + wz;
		dest.m11 = 1f - (xx + zz);
		dest.m12 = yz - wx;
		dest.m20 = xz - wy;
		dest.m21 = yz + wx;
		dest.m22 = 1f - (xx + yy);
	}
}
