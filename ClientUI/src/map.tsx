import ReactMapboxGl from 'react-mapbox-gl'
import {useState,useEffect} from 'react'
import {Markers} from './markers'
import { getDoc } from './getDoc'
import { GetDeliveryInfo } from './getDeliveryInfo'

export function Map({token}:{token:string}){
    const [center,setCenter] = useState<Coordinates>({lat:null,long:null})
    const [zoom, setZoom] = useState(15)
    const {deliveryData,isError} = GetDeliveryInfo(token)
    
    if(isError) return <div>failed to load</div>
    if (!deliveryData) return <div>loading...</div>
    const state = deliveryData.state

    const Map = ReactMapboxGl({
      accessToken:
        'pk.eyJ1IjoiYTQ0ODE2IiwiYSI6ImNrcmF2YnRmMzFsOHoycGxwb3h1bGswc2sifQ.qJqfMCqIvQ_5ctcCo2v4qg'
    }); 
/*
    useEffect(()=>{
      setStatus(deliveryData)
      },[])
        
        collections.pending.doc(token).get()
        .then((doc)=>{
          if(!doc.exists){
            setIsSearching(false)
          }
        })

        collections.pending.onSnapshot((docSnapShot:any)=>{
          docSnapShot.docChanges().forEach((change:any)=>{
            if(change.type==='removed'&& change.doc.id===token){
              setIsSearching(false)
            }
          })
        }, err => {
          console.log(`Encountered error: ${err}`);
        })

        collections.delivering.onSnapshot((docSnapShot:any)=>{
          docSnapShot.docChanges().forEach((change:any)=>{
            if(change.type==='removed'&& change.doc.data().delivery.id===token){
              mutateData()
              if(deliveryData.state==='CANCELLED')
                setIsCancelled(true)
              else if(deliveryData.state==='DELIVERED'){
                setIsDelivered(true)
              }
            }
          })
        }, err => {
          console.log(`Encountered error: ${err}`);
        })
      }
    },[])
*/
    useEffect(()=>{
      if(state==='DELIVERED'){
        setMapCenter()
      }
      async function setMapCenter(){
        const data = await getDoc(token)
        if(!data.empty){
          data.forEach(doc => {
            setCenter({lat:doc.data().location.latitude,long:doc.data().location.longitude})
          }); 
        }
      }
    },[deliveryData])
    
return(
  <div>
    {(() => {
      if(state==='LOOKING_FOR_WARPER'){
        return <p>Searching...</p>
      }
      else if(state==='CANCELLED'){
        return <p>Your order has been cancelled by the store</p>
      }
      else if(state==='DELIVERED'){
        return <p>Your order has arrived!</p>
      }
      else{
        return <div>
            <Map
              style="mapbox://styles/mapbox/streets-v11"
              containerStyle={{
                height: '90vh',
                width: '100vw'
              }}
              center={[center.long,center.lat]}
              zoom={[zoom]}
            >
            <Markers id={token} data={deliveryData}/>
            </Map>
          </div>
      }
})()}
  </div>
)
}