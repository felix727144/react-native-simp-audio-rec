# react-native-simp-audio-rec

implement android.media.AudioRecord

IOS just not implemented
## Installation

```sh
npm install react-native-simp-audio-rec
```

## Usage

```js
import { multiply } from 'react-native-simp-audio-rec';

// ...

SimpleAudioRecord.checkPermission()


this.rec = new SimpleAudioRecord()
this.rec?.addRecordBackListener((data) => {
  //console.log('record callback', data.size, data.currentPosition, this.socket)
  this.socket?.send(Buffer.from(data.data));
})


this.rec?.start();

this.rec?.stop();
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

BSD 2-Clause License

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
