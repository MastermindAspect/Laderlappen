using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Google.Cloud.Firestore;

namespace WebAppLaderLappen.Data
{
    [FirestoreData]
    public class Position
    {
        [FirestoreProperty]
        public double XCord { get; set; }
        [FirestoreProperty]
        public double YCord { get; set; }
        //[FirestoreProperty]
        //public DateTime Date { get; set; }
        
    }
}
