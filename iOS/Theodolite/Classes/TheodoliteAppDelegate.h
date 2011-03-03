//
//  TheodoliteAppDelegate.h
//  Theodolite
//
//  Created by John Fertitta on 3/2/11.
//  Copyright 2011 UMass Lowell. All rights reserved.
//

#import <UIKit/UIKit.h>

@class TheodoliteViewController;

@interface TheodoliteAppDelegate : NSObject <UIApplicationDelegate> {
    UIWindow *window;
    TheodoliteViewController *viewController;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) IBOutlet TheodoliteViewController *viewController;

@end

