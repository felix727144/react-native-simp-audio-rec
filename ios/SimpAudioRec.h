
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNSimpAudioRecSpec.h"

@interface SimpAudioRec : NSObject <NativeSimpAudioRecSpec>
#else
#import <React/RCTBridgeModule.h>

@interface SimpAudioRec : NSObject <RCTBridgeModule>
#endif

@end
