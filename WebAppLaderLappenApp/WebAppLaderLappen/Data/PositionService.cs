using System;
using System.Collections.Generic;
using System.Linq;
using WebAppLaderLappen.Data;
using Google.Cloud.Firestore;
using Newtonsoft.Json;
using System.Threading.Tasks;

namespace WebAppLaderLappen.Data
{
    public class PositionService
    {
        string projectId;
        FirestoreDb fireStoreDb;
        DocumentReference docRef;
        public PositionService()
        {
            string filepath = "Properties/batman-1ca11-firebase-adminsdk-jcuez-a5b0711b70.json";
            Environment.SetEnvironmentVariable("GOOGLE_APPLICATION_CREDENTIALS", filepath);
            projectId = "batman-1ca11";
            fireStoreDb = FirestoreDb.Create(projectId);
        }
        public async Task<List<Position>> GetPositionsAsync()
        {
            try
            {
                Query documentTimeStamp = fireStoreDb.Collection("mowerData").OrderByDescending("time").Limit(1);
                    
                QuerySnapshot documentTimeStampSnapshot = await documentTimeStamp.GetSnapshotAsync();

                foreach (DocumentSnapshot documentSnapshot in documentTimeStampSnapshot.Documents)
                {
                    if (documentSnapshot.Exists)
                    {
                        docRef = documentSnapshot.Reference;

                    }
                }

                Query positionData = fireStoreDb.Collection("mowerData").Document(docRef.Id.ToString()).Collection("path");
                QuerySnapshot positionDataSnapshot = await positionData.GetSnapshotAsync();
                List<Position> lstPosition = new List<Position>();

                foreach (DocumentSnapshot documentSnapshot in positionDataSnapshot.Documents)
                {
                    if (documentSnapshot.Exists)
                    {
                        Dictionary<string, object> position = documentSnapshot.ToDictionary();
                        string json = JsonConvert.SerializeObject(position);

                        Position newuser = JsonConvert.DeserializeObject<Position>(json);

                        lstPosition.Add(newuser);
                    }
                }

                List<Position> sortedPositionList = lstPosition;
                return sortedPositionList;
            }
            catch
            {
                throw;
            }
        }

    }

}
