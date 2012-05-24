#include "/usr/local/include/opencv2/highgui/highgui.hpp"
#include "/usr/local/include/opencv2/imgproc/imgproc.hpp"
#include <cv.h>
#include <math.h>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#define CORNER_EIG_1     "Eigenvalue Corner Detection Image 1"
#define CORNER_EIG_2     "Eigenvalue Corner Detection Image 2"
const int MAX_COUNT = 2000;
const int wsize=80;
float numer[MAX_COUNT][MAX_COUNT];
float denom_1[MAX_COUNT][MAX_COUNT];
float denom_2[MAX_COUNT][MAX_COUNT];
float cor[MAX_COUNT][MAX_COUNT];

int minx=wsize;
int miny=wsize;
int subArray_1[wsize][wsize][MAX_COUNT];
int subArray_2[wsize][wsize][MAX_COUNT];
int GetRand(int min, int max);

using namespace cv;
using namespace std;
int main( int argc, char** argv )
{
IplImage* grey_1 = cvLoadImage(argv[1], CV_LOAD_IMAGE_GRAYSCALE);
IplImage* grey_2 = cvLoadImage(argv[2], CV_LOAD_IMAGE_GRAYSCALE);
if (!grey_1 || !grey_2)
{
    printf("Can not open image file(s).\n");
    return -1;
}


int count = 0;

IplImage* eig_1 = cvCreateImage( cvGetSize(grey_1), 32, 1 );
IplImage* temp_1 = cvCreateImage( cvGetSize(grey_1), 32, 1 );
IplImage* eig_2 = cvCreateImage( cvGetSize(grey_2), 32, 1 );
IplImage* temp_2 = cvCreateImage( cvGetSize(grey_2), 32, 1 );
double quality = 0.1;
double min_distance = 50;
count =MAX_COUNT;
CvPoint2D32f corners_1[MAX_COUNT] = {0};
CvPoint2D32f corners_2[MAX_COUNT] = {0};

cvGoodFeaturesToTrack( grey_1, eig_1, temp_1, corners_1, &count, quality, min_distance, 0, 3, 0, 0.04 );
cvGoodFeaturesToTrack( grey_2, eig_2, temp_2, corners_2, &count, quality, min_distance, 0, 3, 0, 0.04 );

int img_1_w=grey_1->width;
int img_1_h=grey_1->height;
int img_2_w=grey_2->width;
int img_2_h=grey_2->height;

int maxx_1=(int)(img_1_w-wsize);
int maxy_1=(int)(img_1_h-wsize);
int maxx_2=(int)(img_2_w-wsize);
int maxy_2=(int)(img_2_h-wsize);

int sum_1[count];
int sum_2[count];
int mean_1[count];
int mean_2[count];

for( int i = 0; i < count; i++) {
    sum_1[i]=0;
    sum_2[i]=0;
    CvRect window;
    CvMat * tmp;

    //window size
    window.x=corners_1[i].x-wsize/2;
    window.y=corners_1[i].y-wsize/2;
    window.height=wsize;
    window.width=wsize;


    if (window.x>minx && window.y>miny && window.x<maxx_1 && window.y<maxy_1){
    tmp =cvCreateMat(window.height, window.width, CV_8UC1);
    CvMat* sub = cvGetSubRect(grey_1,tmp, window);
    for (int j=0; j<wsize ; j++){
		for (int k=0; k<wsize ; k++){
		CvScalar s;
		s=cvGet2D(sub,j,k); // get the (i,j) pixel value
		subArray_1[j][k][i]=s.val[0];
		sum_1[i]=sum_1[i]+subArray_1[j][k][i];
		}
	}

    }
    mean_1[i]=sum_1[i]/wsize/wsize;
   
    window.x=corners_2[i].x-wsize/2;
    window.y=corners_2[i].y-wsize/2;

    if (window.x>minx && window.y>miny && window.x<maxx_2 && window.y<maxy_2){
    tmp =cvCreateMat(window.height, window.width, CV_8UC1);
    CvMat* sub_2 = cvGetSubRect(grey_2,tmp, window);
    for (int j=0; j<wsize ; j++){
		for (int k=0; k<wsize ; k++){
		CvScalar s_2;
		s_2=cvGet2D(sub_2,j,k); // get the (i,j) pixel value
		subArray_2[j][k][i]=s_2.val[0];
		sum_2[i]=sum_2[i]+subArray_2[j][k][i];
		}
	}   
    }
    mean_2[i]=sum_2[i]/wsize/wsize;
	
	
}
memset( sum_1, 0, sizeof(sum_1) );
memset( sum_2, 0, sizeof(sum_2) );
memset( eig_1, 0, sizeof(eig_1) );
memset( temp_1, 0, sizeof(temp_1) );
memset( eig_2, 0, sizeof(eig_2) );
memset( temp_2, 0, sizeof(temp_2) );



for( int i = 0; i < count; i++) {
	for( int m = 0; m < count; m++) {
		numer[i][m]=0;
		denom_1[i][m]=0;
		denom_2[i][m]=0;
		for ( int j = 0; j < wsize; j++){
			for ( int k = 0; k < wsize; k++){
				numer[i][m]=numer[i][m]+(subArray_1[j][k][i]-mean_1[i])*(subArray_2[j][k][m]-mean_2[m]);
				denom_1[i][m]=denom_1[i][m]+(subArray_1[j][k][i]-mean_1[i])*(subArray_1[j][k][i]-mean_1[i]);
				denom_2[i][m]=denom_2[i][m]+(subArray_2[j][k][m]-mean_2[m])*(subArray_2[j][k][m]-mean_2[m]);
			}
		}
		cor[i][m]=numer[i][m]/sqrt(denom_1[i][m])/sqrt(denom_2[i][m]);

	}	      
}


float max_1[count];
int ind_1[count];
for (int i=0; i<count; i++)
   {
		max_1[i]=-32000;
		for (int j=0; j<count; j++)

		   {		
			 if (cor[i][j]>max_1[i])
			 {
			    max_1[i]=cor[i][j];
			    ind_1[i]=j;	
			 }
		   }
 }

float max_2[count];
int ind_2[count];
for (int j=0; j<count; j++)
   {
		max_2[j]=-32000;
		for (int i=0; i<count; i++)
		   {
			 if (cor[i][j]>max_2[j])
			 {
			    max_2[j]=cor[i][j];
			    ind_2[j]=i;	
			 }
		   }
 }

//printf("index=%i\n",r3);
int no_match=0;
int match[count];
for (int i=0; i<count; i++)
   {
	if(ind_2[ind_1[i]]==i){
//    if(ind_1[i]!=0 && cor[i][ind_1[i]]>0.8){

    match[no_match]=i;	
    no_match=no_match+1;

  
	}
}
float p=0.9;
float e=0.5;
float S=6;
float N=log(1-p)/log(1-pow((1-e),S));
int no_inlier[int(N)];
int inlier_index[int(N)][1000];
for (int i=0; i<int(N); i++){
	int r1 = GetRand(1, int(no_match/3));
	int r2 = GetRand(int(no_match/3)+1, int(2*no_match/3));
	int r3 = GetRand(int(2*no_match/3)+1, count);
	int x1=corners_1[match[r1]].x;
	int y1=corners_1[match[r1]].y;
	int x2=corners_1[match[r2]].x;
	int y2=corners_1[match[r2]].y;
	int x3=corners_1[match[r3]].x;
	int y3=corners_1[match[r3]].y;
	int X1=corners_2[ind_1[match[r1]]].x;
	int Y1=corners_2[ind_1[match[r1]]].y;
	int X2=corners_2[ind_1[match[r2]]].x;
	int Y2=corners_2[ind_1[match[r2]]].y;
	int X3=corners_2[ind_1[match[r3]]].x;
	int Y3=corners_2[ind_1[match[r3]]].y;
	

	double a[] = { 1,  1,  1,
		       x1, x2, x3,
		       y1, y2, y3};

	double b[] = { X1, X2, X3,
		       Y1, Y2, Y3};


	CvMat Ma, Mb;

	cvInitMatHeader(&Ma, 3, 3, CV_64FC1, a);
	cvInitMatHeader(&Mb, 2, 3, CV_64FC1, b);
	CvMat *Mc=cvCreateMat(3,3,CV_64FC1);
	CvMat *Md=cvCreateMat(2,3,CV_64FC1);
	cvInvert(&Ma,Mc);
	cvMatMul(&Mb,Mc,Md);
	float a0=cvmGet(Md,0,0);
	float a1=cvmGet(Md,0,1);
	float a2=cvmGet(Md,0,2);
	float b0=cvmGet(Md,1,0);
	float b1=cvmGet(Md,1,1);
	float b2=cvmGet(Md,1,2);
	no_inlier[i]=0;
	for (int j=0; j<no_match; j++){
		int xx=corners_1[match[j]].x;
		int yy=corners_1[match[j]].y;
		int XX1=a0+a1*xx+a2*yy;
		int YY1=b0+b1*xx+b2*yy;
		int XX2=corners_2[ind_1[match[j]]].x;
		int YY2=corners_2[ind_1[match[j]]].y;
		float d=sqrt(pow((XX1-XX2),2)+pow((YY1-YY2),2));
		if (d<100){
			inlier_index[i][no_inlier[i]]=match[j];
			no_inlier[i]=no_inlier[i]+1;	
	
		}
	}
//	printf("index=%i\n",i);
//	printf("inlier_number=%i\n",no_inlier[i]);

}

int maximum = no_inlier[0];
int location;
    for ( int c = 1 ; c < int(N) ; c++ ) 
    {
        if ( no_inlier[c] > maximum ) 
        {
           maximum = no_inlier[c];
           location = c;
        }
    } 
printf("index=%i\n",location);
printf("index=%i\n",maximum);

for (int i=0;i<maximum;i++){
int in_ind=inlier_index[location][i];
int xx_in=corners_1[in_ind].x;
int yy_in=corners_1[in_ind].y;
int XX_in=corners_2[ind_1[in_ind]].x;
int YY_in=corners_2[ind_1[in_ind]].y;
cvCircle(grey_1,cvPoint(xx_in,yy_in), 30, cvScalar(0, 0, 255, 0),30, 8, 0);
cvCircle(grey_2,cvPoint(XX_in,YY_in), 30, cvScalar(0, 0, 255, 0),30, 8, 0);  
}
// Release the capture device housekeeping
cvNamedWindow(CORNER_EIG_1, 0); // allow the window to be resized
cvShowImage(CORNER_EIG_1, grey_1);
cvNamedWindow(CORNER_EIG_2, 0); // allow the window to be resized
cvShowImage(CORNER_EIG_2, grey_2);
cvWaitKey(0);
cvDestroyWindow(CORNER_EIG_1);
cvDestroyWindow(CORNER_EIG_2);
cvReleaseImage( &grey_1);
cvReleaseImage( &grey_2);

}

int GetRand(int min, int max)
{
  static int Init = 0;
  int rc;
  
  if (Init == 0)
  {
    /*
     *  As Init is static, it will remember it's value between
     *  function calls.  We only want srand() run once, so this
     *  is a simple way to ensure that happens.
     */
    srand(time(NULL));
    Init = 1;
  }

  /*
   * Formula:  
   *    rand() % N   <- To get a number between 0 - N-1
   *    Then add the result to min, giving you 
   *    a random number between min - max.
   */  
  rc = (rand() % (max - min + 1) + min);
  
  return (rc);
}



