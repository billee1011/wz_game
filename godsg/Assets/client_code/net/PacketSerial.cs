
using System;
using System.IO;
using P3Net;

public class PacketSerial
{
	#region Read
	public static Int16 ReadInt16(BinaryReader iStream)
	{
		return iStream.ReadInt16();
	}

	public static Int32 ReadInt32(BinaryReader iStream)
	{
		return iStream.ReadInt32();
	}

	public static Int64 ReadInt64(BinaryReader iStream)
	{
		return iStream.ReadInt64();
	}

	public static UInt16 ReadUInt16(BinaryReader iStream)
	{
		return iStream.ReadUInt16();
	}

	public static UInt32 ReadUInt32(BinaryReader iStream)
	{
		return iStream.ReadUInt32();
	}

	public static UInt64 ReadUInt64(BinaryReader iStream)
	{
		return iStream.ReadUInt64();
	}

	public static string ReadString(BinaryReader iStream)
	{
		Int32 tmpSize = iStream.ReadInt32();
		return PacketUtil.ReadString(iStream, tmpSize);
	}

	public static string ReadFixedString(BinaryReader iStream, Int32 size)
	{
		string tmp = PacketUtil.ReadString(iStream, size);
		//处理服务器发来的不正常数据
		int nTmp = tmp.IndexOf("\0");
		if (nTmp > -1)
		{
			tmp = tmp.Substring(0, nTmp);
		}
		return tmp;
	}

	public static bool ReadBoolean(BinaryReader iStream)
	{
		return iStream.ReadBoolean();
	}

	public static byte ReadByte(BinaryReader iStream)
	{
		return iStream.ReadByte();
	}

	public static char ReadChar(BinaryReader iStream)
	{
		return iStream.ReadChar();
	}

	public static float ReadSingle(BinaryReader iStream)
	{
		return iStream.ReadSingle();
	}

	public static double ReadDouble(BinaryReader iStream)
	{
		return iStream.ReadDouble();
	}
	#endregion

	#region Write
	public static void Write(BinaryWriter oStream, bool value)
	{
		if (oStream == null)
		{
			return;
		}

		oStream.Write(value);
	}

	public static void Write(BinaryWriter oStream, byte value)
	{
		if (oStream == null)
		{
			return;
		}

		oStream.Write(value);
	}

	public static void Write(BinaryWriter oStream, char value)
	{
		if (oStream == null)
		{
			return;
		}

		oStream.Write(value);
	}

	public static void Write(BinaryWriter oStream, char[] value)
	{
		if (oStream == null)
		{
			return;
		}

		oStream.Write(value);
	}

	public static void Write(BinaryWriter oStream, decimal value)
	{
		if (oStream == null)
		{
			return;
		}

		oStream.Write(value);
	}

	public static void Write(BinaryWriter oStream, double value)
	{
		if (oStream == null)
		{
			return;
		}

		oStream.Write(value);
	}

	public static void Write(BinaryWriter oStream, float value)
	{
		if (oStream == null)
		{
			return;
		}

		oStream.Write(value);
	}

	public static void Write(BinaryWriter oStream, int value)
	{
		if (oStream == null)
		{
			return;
		}

		oStream.Write(value);
	}

	public static void Write(BinaryWriter oStream, long value)
	{
		if (oStream == null)
		{
			return;
		}

		oStream.Write(value);
	}

	public static void Write(BinaryWriter oStream, sbyte value)
	{
		if (oStream == null)
		{
			return;
		}

		oStream.Write(value);
	}

	public static void Write(BinaryWriter oStream, short value)
	{
		if (oStream == null)
		{
			return;
		}

		oStream.Write(value);
	}

	public static void Write(BinaryWriter oStream, string value, bool addLength = true)
	{
		if (oStream == null)
		{
			return;
		}

		Int32 strLength = PacketUtil.GetUTF8StringLength(value);
		if (addLength)
		{
			oStream.Write(strLength);
		}

		if (strLength > 0)
		{
			PacketUtil.WriteString(oStream, value);
		}
	}

	public static void Write(BinaryWriter oStream, uint value)
	{
		if (oStream == null)
		{
			return;
		}

		oStream.Write(value);
	}

	public static void Write(BinaryWriter oStream, ulong value)
	{
		if (oStream == null)
		{
			return;
		}

		oStream.Write(value);
	}

	public static void Write(BinaryWriter oStream, ushort value)
	{
		if (oStream == null)
		{
			return;
		}

		oStream.Write(value);
	}
	#endregion
}