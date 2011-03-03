//
//  TheodoliteViewController.m
//  Theodolite
//
//  Created by John Fertitta on 3/2/11.
//  Copyright 2011 UMass Lowell. All rights reserved.
//

#import "TheodoliteViewController.h"

@implementation TheodoliteViewController

@synthesize muteSwitch;  

static double const TIME_INTERVAL = 0.1;

- (void)didReceiveMemoryWarning {
	// Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
	
	// Release any cached data, images, etc that aren't in use.
}


- (void)viewDidUnload {
	// Release any retained subviews of the main view.
	// e.g. self.myOutlet = nil;
}


- (void)dealloc {
	[audioPlayer release];
    [super dealloc];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
	[textField resignFirstResponder];
	return NO;
}

- (void)accelerometer:(UIAccelerometer *)acel didAccelerate:(UIAcceleration *)acceleration
{
	// Create Status feedback string
	angle = atan2(acceleration.y, acceleration.z) + M_PI;
	NSString *xstring = [NSString stringWithFormat: @"%f degrees", angle * 180/M_PI];
	if (isRunning) {
		[angleDisplay setText:xstring];
		CGFloat distance = [[distanceInput text] floatValue];
		altitude = distance * tan(angle);
		NSString *altitudeString = [NSString stringWithFormat:@"%f", altitude];
		[altitudeDisplay setText:altitudeString];
	}
	
}

-(IBAction)startPressed:(UIButton *)sender {
	isRunning = !isRunning;
	
	AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
	
	if (isRunning) {
		accel = [UIAccelerometer sharedAccelerometer];
		accel.delegate = self;
		accel.updateInterval = 1.0f/60.0f;   
		
		time = 0;
		ticks = 0;

		timer = [[NSTimer scheduledTimerWithTimeInterval: TIME_INTERVAL 
													target: self 
													selector: @selector(timerFired:) 
													userInfo: nil
													repeats: YES] retain];
	} else {
		[timer invalidate];
		[timer release];
		timer = nil;
	}
}

-(IBAction)toggleEnabledForMuteSwitch:(id) sender {
	isMuted = !muteSwitch.on;
}


- (void) timerFired:(NSTimer *) theTimer {
	time += TIME_INTERVAL;
	ticks += TIME_INTERVAL;
	NSString *timeString = [NSString stringWithFormat:@"%f", time];
	[timeDisplay setText:timeString];
	
	if (ticks >= 1) {
		if (!isMuted) {
			NSURL *url = [NSURL fileURLWithPath:[NSString stringWithFormat:@"%@/beep.mp3", [[NSBundle mainBundle] resourcePath]]];
		
			NSError *error;
			audioPlayer = [[AVAudioPlayer alloc] initWithContentsOfURL:url error:&error];
			audioPlayer.numberOfLoops = 0;
		
			if (audioPlayer == nil)
				NSLog([error description]);
			else
				[audioPlayer play];
			}
		ticks = 0;
	}
}

@end
