using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace WzNet
{
    public class RequestMessage : BaseMessage
    {
        private byte moduleId;

        private byte actionId;

        private byte[] protoMessage;

        public RequestMessage(byte moduleId , byte actionId , byte[] message)
        {
            this.moduleId = moduleId;
            this.actionId = actionId;
            this.protoMessage = message;
        }

        public RequestMessage(byte moduleId, byte actionId):this(moduleId , actionId , null)
        {
        }

        public override byte[] getBytes()
        {
            int length = 2 + (protoMessage == null ? 0 : protoMessage.Length);
            ByteBuffer buffer = new ByteBuffer();
            buffer.writeInt(length);
            buffer.writeByte(moduleId);
            buffer.writeByte(actionId);
            if(protoMessage!= null)
            {
                buffer.writeBytes(protoMessage);
            }
            return buffer.getWriteBytes();
        }
        


    }
}
