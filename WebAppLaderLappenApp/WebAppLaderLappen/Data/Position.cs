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
        public bool collision { get; set; }
        [FirestoreProperty]
        public bool onLine { get; set; }
        [FirestoreProperty]
        public double x { get; set; }
        [FirestoreProperty]
        public double y { get; set; }
        //[FirestoreProperty]
        //public DateTime Date { get; set; }

    }
}
