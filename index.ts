import { NativeModules, Platform } from 'react-native';
import type { EmitterSubscription } from 'react-native';
import {
  DeviceEventEmitter,
  NativeEventEmitter,
} from 'react-native';

const { SimpAudioRec } = NativeModules

export type RecordBackType = {
  isRecording?: boolean;
  currentPosition: number;
  currentMetering?: number;
};

export type PlayBackType = {
  isMuted?: boolean;
  currentPosition: number;
  duration: number;
};

class SimpleAudioRecord {
  private _recorderSubscription?: EmitterSubscription;
  constructor(){
    SimpAudioRec.multiply(10,50)
  }

  start = () => {
    //console.log('SimpAudioRec:', SimpAudioRec)
    SimpAudioRec.start()
  }
  stop=()=>{
    //console.log('invoke SimpAudioRec.stop')
    SimpAudioRec.stop()
  }

  static checkPermission = async () => {
    try{
      await SimpAudioRec.checkPermission()
    }catch(e){
      console.log(e)
    }
  }
  addRecordBackListener = (
    callback: (recordingMeta: RecordBackType) => void,
  ): void => {
    if (Platform.OS === 'android') {
      this._recorderSubscription = DeviceEventEmitter.addListener(
        'sar-back',
        callback,
      );
    } else {
      const myModuleEvt = new NativeEventEmitter(SimpAudioRec);

      this._recorderSubscription = myModuleEvt.addListener(
        'sar-back',
        callback,
      );
    }
  };

  /**
   * Remove listener for recorder.
   * @returns {void}
   */
  removeRecordBackListener = (): void => {
    if (this._recorderSubscription) {
      this._recorderSubscription.remove();
      this._recorderSubscription = undefined;
    }
  };
}

export default SimpleAudioRecord;
