resource "aws_s3_bucket" "photos" {
  bucket_prefix = "${var.name_prefix}-profile-photos-"
  force_destroy = true # Course-only: destroys uploaded images with terraform destroy.
}
resource "aws_s3_bucket_public_access_block" "photos" {
  bucket                  = aws_s3_bucket.photos.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
resource "aws_s3_bucket_server_side_encryption_configuration" "photos" {
  bucket = aws_s3_bucket.photos.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}
resource "aws_iam_role" "backend_photos" {
  name_prefix        = "${var.name_prefix}-photos-"
  assume_role_policy = jsonencode({ Version = "2012-10-17", Statement = [{ Effect = "Allow", Principal = { Service = "ec2.amazonaws.com" }, Action = "sts:AssumeRole" }] })
}
resource "aws_iam_role_policy" "backend_photos" {
  role   = aws_iam_role.backend_photos.id
  policy = jsonencode({ Version = "2012-10-17", Statement = [{ Effect = "Allow", Action = ["s3:PutObject", "s3:GetObject", "s3:DeleteObject"], Resource = "${aws_s3_bucket.photos.arn}/profiles/*" }] })
}
resource "aws_iam_instance_profile" "backend_photos" {
  name_prefix = "${var.name_prefix}-photos-"
  role        = aws_iam_role.backend_photos.name
}
output "photo_bucket_name" { value = aws_s3_bucket.photos.id }
