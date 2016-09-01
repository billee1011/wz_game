using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using ProtoBuf;
using System.IO;

namespace proto
{
    public class ServerMessageCreator
    {

        public static Stream createAccountLogin(string userName, string password)
        {
            AccountLogin pb = new AccountLogin();
            pb.username = userName;
            pb.password = password;
            Stream stream = new MemoryStream();
            Serializer.Serialize<AccountLogin>(stream , pb);
            stream.Position = 0L;
            return stream;
        }
    }
}
