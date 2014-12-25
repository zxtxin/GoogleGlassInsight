attribute float pixel_point;
void main()
{
    gl_Position = vec4( pixel_point * 0.0078125 -1.0 , 0.0 , 0.0 , 1.0);
    gl_PointSize = 1.0;
}