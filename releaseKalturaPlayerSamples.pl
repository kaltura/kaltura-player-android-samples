#!/usr/bin/env perl 

use strict;

my $num_args = $#ARGV + 1;

if ($num_args != 3) {
    print "\nUsage: $0 \$pathToSamplesFolder \$oldVer \$newVer\n\n";
    print "Example: $0 /Users/rony/Dev/android/kaltura/kaltura-player-android-samples 4.4.0 4.5.0\n\n";
    exit;
}

my ($pathToSamplesFolder, $oldVer, $newVer) = @ARGV;
print "Relase Android Samples\n";

my @files = `find $pathToSamplesFolder/*/* | grep build.gradle`;

#my @files = `find $ENV{'HOME'}/Dev/android/kaltura/kaltura-player-android-samples/*/* | grep build.gradle`;


foreach my $file(@files) {
   print "Updating $file\n";
   open INFILE, "$file"; 
   my @data = <INFILE>; 
   close INFILE ;
   
   my @updatedFileLines = ();
   foreach my $line(@data) {
      if ($line =~ /implementation 'com.kaltura.playkit/) {
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
  
  # system ("cat $file");
}

opendir(my $DIR, $pathToSamplesFolder);
while ( my $entry = readdir $DIR ) {
    next unless -d $pathToSamplesFolder . '/' . $entry;
    next if $entry eq '.' or $entry eq '..';
    next if $entry  =~ /^\./;
    print "Found directory <$pathToSamplesFolder/$entry>\n";
    chdir ("$pathToSamplesFolder/$entry");
    my $returnCode = system("./gradlew build");
    if ($returnCode != 0) { 
        die "Failed executing [./gradlew biuld]\n"; 
        closedir $DIR;
    } 
}
closedir $DIR;

