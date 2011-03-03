//
//  TheodoliteViewController.h
//  Theodolite
//
//  Created by John Fertitta on 3/2/11.
//  Copyright 2011 UMass Lowell. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AudioToolbox/AudioToolbox.h>
#import <AVFoundation/AVFoundation.h>

@interface TheodoliteViewController : UIViewController <UIAccelerometerDelegate> {
	IBOutlet UILabel *angleDisplay;
	IBOutlet UILabel *altitudeDisplay;
	IBOutlet UILabel *timeDisplay;
	IBOutlet UITextField *distanceInput;
	IBOutlet UISwitch *muteSwitch;
	//IBOutlet UITextField *distanceInput;
	UIAccelerometer *accel;
	NSTimer *timer;
	double time;
	BOOL isRunning;
	CGFloat angle;
	CGFloat altitude;
	double ticks;
	BOOL isMuted;
	
	AVAudioPlayer *audioPlayer;
}
@property (nonatomic, retain) UISwitch *muteSwitch;  

-(IBAction)toggleEnabledForMuteSwitch:(id) sender;  
-(IBAction)startPressed:(UIButton *)sender;
@end

