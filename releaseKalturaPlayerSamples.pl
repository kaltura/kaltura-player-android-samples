#!/usr/bin/env perl 

use strict;

my $num_args = $#ARGV + 1;

if ($num_args != 3) {
    print "\nUsage: $0 \$pathToSamplesFolder \$oldVer \$newVer\n\n";
    print "Example: $0 /Users/rony/Dev/android/kaltura/kaltura-player-android-samples 4.4.0 4.5.0\n\n";
    exit;
}

my ($pathToSamplesFolder, $oldVer, $newVer) = @ARGV;
print "Release Android Samples\n";

my @files = `find $pathToSamplesFolder/*/* | grep version.gradle`;

#my @files = `find $ENV{'HOME'}/Dev/android/kaltura/kaltura-player-android-samples/*/* | grep build.gradle`;


foreach my $file(@files) {
   print "Updating $file\n";
   open INFILE, "$file"; 
   my @data = <INFILE>; 
   close INFILE ;
   
   my @updatedFileLines = ();
   foreach my $line(@data) {
      if ($line =~ /def playerVersion = /) {
           $line =~ s/$oldVer/$newVer/;
      }
      push(@updatedFileLines, $line);
   }

   open (EMPTYFILE, ">$file");
   close EMTPTYFILE;

   open (OUTFILE, ">>$file");
   foreach my $newLine(@updatedFileLines) {
##        print  $newLine;
         print OUTFILE $newLine;
   }
   close OUTFILE;
  `git add $file`  
  # system ("cat $file");
}

opendir(my $DIR, $pathToSamplesFolder);
while ( my $entry = readdir $DIR ) {
    next unless -d $pathToSamplesFolder . '/' . $entry;
    next if $entry eq '.' or $entry eq '..';
    next if $entry  =~ /^\./;
    print "Found directory <$pathToSamplesFolder/$entry>\n";
    chdir ("$pathToSamplesFolder/$entry");
    my $returnCode = system("./gradlew build --no-daemon");
    if ($returnCode != 0) { 
        die "Failed executing [./gradlew build --no-daemon]\n"; 
        closedir $DIR;
    } 
}
closedir $DIR;

